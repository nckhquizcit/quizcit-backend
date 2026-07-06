package vn.id.quiz.quizcitbackend.modules.ai.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.id.quiz.quizcitbackend.modules.ai.client.AIClient;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaBatchRequest;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaBatchResponse;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaRequest;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaResponse;
import vn.id.quiz.quizcitbackend.modules.ai.utils.ThetaUtils;

/**
 * Service ước lượng năng lực (theta) cho người dùng dựa trên các phản hồi
 * trong lịch sử làm bài.
 *
 * <p>Là lớp trung gian giữa controller và {@link AIClient}. Giữ lại để
 * có thể cache, log hoặc fallback (nếu AI Service ngoại tuyến) mà không
 * ảnh hưởng tầng presentation.</p>
 *
 * <p><b>Đồng bộ thuật toán:</b> Fallback nội bộ ({@link #computeThetaLocally})
 * được viết bám sát {@code AI/main.py -> rasch_theta} (Newton-Raphson,
 * prior N(0, sigma^2) với sigma = 1) để khi AI Service offline/chậm, kết
 * quả theta gần như trùng khớp:
 * <ul>
 *   <li>Cùng {@code sigma = 1} → {@code PRIOR_VARIANCE = 1.0}.</li>
 *   <li>Cùng {@code max_iter = 20} và ngưỡng hội tụ {@code 1e-4}.</li>
 *   <li>Clamp theta về {@code [-4, 4]} mỗi vòng lặp (giống {@code np.clip}
 *       trong Python), nhưng KHÔNG round giữa chừng — chỉ round/scale khi
 *       trả cho FE qua {@link ThetaUtils#toScaled}.</li>
 * </ul>

 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    private static final double DEFAULT_THETA = 0.0;
    // sigma = 1 -> PRIOR_VARIANCE = sigma^2 = 1.0.
    // Phải khớp với AI Service (AI/main.py -> rasch_theta, sigma = 1)
    // để fallback nội bộ cho ra theta tương đương khi AI Service offline/chậm.
    private static final double PRIOR_VARIANCE = 1.0;
    private static final int MAX_ITERATIONS = 20;
    private static final double CONVERGENCE_THRESHOLD = 1.0e-4;
    // Clamp theta về [-4, 4] mỗi vòng lặp, khớp np.clip trong AI/main.py.
    private static final double THETA_MIN = -4.0;
    private static final double THETA_MAX = 4.0;


    private final AIClient aiClient;

    /**
     * Ước lượng theta cho MỘT skill. Ưu tiên gọi AI Service; nếu lỗi/timeout
     * thì fallback sang thuật toán IRT nội bộ.
     */
    public EstimateThetaResponse estimateTheta(EstimateThetaRequest request) {
        try {
            EstimateThetaResponse response = aiClient.estimateTheta(request);
            if (response != null) {
                return withScaledTheta(response.theta());
            }
        } catch (RuntimeException ex) {
            log.warn("AI theta service unavailable, using local IRT fallback: {}", ex.getMessage());
        }

        return withScaledTheta(computeThetaLocally(
                request == null ? null : request.responses(),
                request == null ? null : request.difficulties(),
                request == null ? null : request.weights()));
    }

    /**
     * Ước lượng theta cho NHIỀU skill trong 1 lần gọi (batch).
     *
     * <p>Thay cho việc gọi {@link #estimateTheta} lặp lại cho từng skill —
     * loại bỏ chi phí N lần round-trip HTTP (đặc biệt nặng khi AI Service
     * cold start). Nếu batch endpoint lỗi/timeout, toàn bộ item sẽ được
     * tính bằng fallback nội bộ.</p>
     *
     * @param items map {@code skillId -> dữ liệu theta}
     * @return map {@code skillId -> theta raw}
     */
    public Map<String, Double> estimateThetaBatch(Map<String, EstimateThetaRequest> items) {
        Map<String, Double> result = new LinkedHashMap<>();
        if (items == null || items.isEmpty()) {
            return result;
        }

        List<EstimateThetaBatchRequest.Item> payload = new ArrayList<>(items.size());
        for (Map.Entry<String, EstimateThetaRequest> entry : items.entrySet()) {
            EstimateThetaRequest req = entry.getValue();
            payload.add(new EstimateThetaBatchRequest.Item(
                    entry.getKey(),
                    req.responses(),
                    req.difficulties(),
                    req.weights()));
        }

        try {
            EstimateThetaBatchResponse response = aiClient.estimateThetaBatch(
                    new EstimateThetaBatchRequest(payload));

            if (response != null && response.results() != null) {
                for (EstimateThetaBatchResponse.Item item : response.results()) {
                    if (item != null && item.key() != null) {
                        result.put(item.key(), item.theta());
                    }
                }
            }
        } catch (RuntimeException ex) {
            log.warn("AI theta batch service unavailable, using local IRT fallback: {}",
                    ex.getMessage());
        }

        // Bù các skill mà AI Service không trả về (batch lỗi 1 phần hoặc toàn bộ):
        // tính bằng fallback nội bộ để đảm bảo mọi skill đều có theta.
        for (Map.Entry<String, EstimateThetaRequest> entry : items.entrySet()) {
            result.computeIfAbsent(entry.getKey(), key -> {
                EstimateThetaRequest req = entry.getValue();
                return computeThetaLocally(
                        req.responses(),
                        req.difficulties(),
                        req.weights());
            });
        }

        return result;
    }

    /**
     * Bản Java của {@code AI/main.py -> rasch_theta}. Giữ nguyên trình tự
     * Newton-Raphson: tính gradient/hessian, cập nhật theta, clamp về
     * {@code [-4, 4]} rồi kiểm tra hội tụ trên {@code |new_theta - theta|}.
     */

    double computeThetaLocally(
            List<Integer> responses,
            List<Double> difficulties,
            List<Double> weights) {

        int itemCount = usableItemCount(responses, difficulties, weights);
        if (itemCount == 0) {
            return DEFAULT_THETA;
        }

        double theta = DEFAULT_THETA;

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            double gradient = 0.0;
            double hessian = 0.0;

            for (int index = 0; index < itemCount; index++) {
                int response = sanitizeResponse(responses.get(index));
                double difficulty = finiteOrDefault(difficulties.get(index), DEFAULT_THETA);
                double weight = positiveOrDefault(weights.get(index), 1.0);
                double probability = probability(theta, difficulty);

                gradient += weight * (response - probability);
                hessian -= weight * probability * (1.0 - probability);
            }

            gradient -= theta / PRIOR_VARIANCE;
            hessian -= 1.0 / PRIOR_VARIANCE;

            double newTheta = theta - gradient / hessian;
            // clamp về [-4, 4] mỗi vòng lặp, khớp np.clip trong AI/main.py.
            newTheta = Math.max(THETA_MIN, Math.min(THETA_MAX, newTheta));

            if (Math.abs(newTheta - theta) < CONVERGENCE_THRESHOLD) {

                theta = newTheta;
                break;
            }

            theta = newTheta;
        }

        return theta;
    }

    private int usableItemCount(
            List<Integer> responses,
            List<Double> difficulties,
            List<Double> weights) {

        if (responses == null || difficulties == null || weights == null) {
            return 0;
        }

        return Math.min(responses.size(), Math.min(difficulties.size(), weights.size()));
    }

    private EstimateThetaResponse withScaledTheta(double theta) {
        return new EstimateThetaResponse(theta, ThetaUtils.toScaled(theta));
    }

    private int sanitizeResponse(Integer response) {
        return response != null && response > 0 ? 1 : 0;
    }

    private double finiteOrDefault(Double value, double fallback) {
        return value != null && Double.isFinite(value) ? value : fallback;
    }

    private double positiveOrDefault(Double value, double fallback) {
        return value != null && Double.isFinite(value) && value > 0.0 ? value : fallback;
    }

    private double probability(double theta, double difficulty) {
        double exponent = Math.max(-35.0, Math.min(35.0, -(theta - difficulty)));
        return 1.0 / (1.0 + Math.exp(exponent));
    }
}

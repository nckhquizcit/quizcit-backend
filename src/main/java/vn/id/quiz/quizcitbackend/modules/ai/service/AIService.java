package vn.id.quiz.quizcitbackend.modules.ai.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.id.quiz.quizcitbackend.modules.ai.client.AIClient;
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
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    private static final double DEFAULT_THETA = 0.0;
    // sigma = 1 -> PRIOR_VARIANCE = sigma^2 = 1.0.
    // Phải khớp với AI Service (quizcit-ai/main.py -> rasch_theta, sigma = 1)
    // để fallback nội bộ cho ra theta tương đương khi AI Service offline/chậm.
    private static final double PRIOR_VARIANCE = 1.0;
    private static final int MAX_ITERATIONS = 20;
    private static final double CONVERGENCE_THRESHOLD = 1.0e-4;
    private static final double MIN_HESSIAN_ABS = 1.0e-9;
    private static final double THETA_MIN = -4.0;
    private static final double THETA_MAX = 4.0;

    private final AIClient aiClient;

    public EstimateThetaResponse estimateTheta(EstimateThetaRequest request) {
        try {
            EstimateThetaResponse response = aiClient.estimateTheta(request);
            if (response != null) {
                return withScaledTheta(response.theta());
            }
        } catch (RuntimeException ex) {
            log.warn("AI theta service unavailable, using local IRT fallback: {}", ex.getMessage());
        }

        return estimateThetaLocally(request);
    }

    private EstimateThetaResponse estimateThetaLocally(EstimateThetaRequest request) {
        int itemCount = countUsableItems(request);
        if (itemCount == 0) {
            return withScaledTheta(DEFAULT_THETA);
        }

        double theta = DEFAULT_THETA;

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            double gradient = 0.0;
            double hessian = 0.0;

            for (int index = 0; index < itemCount; index++) {
                int response = sanitizeResponse(request.responses().get(index));
                double difficulty = finiteOrDefault(request.difficulties().get(index), DEFAULT_THETA);
                double weight = positiveOrDefault(request.weights().get(index), 1.0);
                double probability = probability(theta, difficulty);

                gradient += weight * (response - probability);
                hessian -= weight * probability * (1.0 - probability);
            }

            gradient -= theta / PRIOR_VARIANCE;
            hessian -= 1.0 / PRIOR_VARIANCE;

            if (Math.abs(hessian) < MIN_HESSIAN_ABS) {
                break;
            }

            double step = gradient / hessian;
            theta = ThetaUtils.toRaw(ThetaUtils.toScaled(theta - step));

            if (Math.abs(step) < CONVERGENCE_THRESHOLD) {
                break;
            }
        }

        return withScaledTheta(theta);
    }

    private int countUsableItems(EstimateThetaRequest request) {
        if (request == null) {
            return 0;
        }

        List<Integer> responses = request.responses();
        List<Double> difficulties = request.difficulties();
        List<Double> weights = request.weights();

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

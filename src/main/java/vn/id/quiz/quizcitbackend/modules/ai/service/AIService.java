package vn.id.quiz.quizcitbackend.modules.ai.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.id.quiz.quizcitbackend.modules.ai.client.AIClient;
import vn.id.quiz.quizcitbackend.modules.ai.dto.ChatRequest;
import vn.id.quiz.quizcitbackend.modules.ai.dto.ChatResponse;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EmbedRequest;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EmbedResponse;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaBatchRequest;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaBatchResponse;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaRequest;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaResponse;
import vn.id.quiz.quizcitbackend.modules.ai.dto.GenerateAdviceRequest;
import vn.id.quiz.quizcitbackend.modules.ai.dto.SkillDeltaItem;
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
 *
 * <p><b>Chatbox + lời khuyên:</b> Đây là các tính năng phụ thuộc LLM
 * (Gemini) nên KHÔNG có fallback "tương đương" như Rasch — thay vào đó:
 * <ul>
 *   <li>{@link #embedTexts}: fail thì trả {@code null}, để caller tự quyết
 *       định (chatbot fallback sang tìm kiếm từ khóa, indexing thì bỏ qua và
 *       log để admin reindex lại sau).</li>
 *   <li>{@link #chat}: fail thì trả câu xin lỗi an toàn — chat luôn phải có
 *       phản hồi, không được để lỗi 5xx dội lên UI.</li>
 *   <li>{@link #generateAdvice}: fail thì fallback sang lời khuyên rule-based
 *       dựa thẳng trên delta theta (không cần LLM).</li>
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

    private static final String CHAT_FALLBACK_MESSAGE =
            "Xin lỗi, trợ lý AI đang tạm thời không phản hồi được. Bạn vui lòng thử lại sau ít phút.";


    private final AIClient aiClient;

    /**
     * Ước lượng theta cho MỘT skill. Ưu tiên gọi AI Service; nếu lỗi/timeout
     * thì fallback sang thuật toán IRT nội bộ.
     */
    public EstimateThetaResponse estimateTheta(EstimateThetaRequest request) {
        try {
            EstimateThetaResponse response = aiClient.estimateTheta(request);
            if (response != null) {
                return response;
            }
        } catch (RuntimeException ex) {
            log.warn("AI theta service unavailable, using local IRT fallback: {}", ex.getMessage());
        }

        return computeThetaLocally(
                request == null ? DEFAULT_THETA : request.oldTheta(),
                request == null ? 1.0 : request.oldThetaVar(),
                request == null ? null : request.responses(),
                request == null ? null : request.difficulties(),
                request == null ? null : request.weights());
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
    public Map<String, EstimateThetaResponse> estimateThetaBatch(Map<String, EstimateThetaRequest> items) {
        Map<String, EstimateThetaResponse> result = new LinkedHashMap<>();
        if (items == null || items.isEmpty()) {
            return result;
        }

        List<EstimateThetaBatchRequest.Item> payload = new ArrayList<>(items.size());
        for (Map.Entry<String, EstimateThetaRequest> entry : items.entrySet()) {
            EstimateThetaRequest req = entry.getValue();
            payload.add(new EstimateThetaBatchRequest.Item(
                    entry.getKey(),
                    req.oldTheta(),
                    req.oldThetaVar(),
                    req.responses(),
                    req.difficulties(),
                    req.weights()));
        }

        try {
            EstimateThetaBatchResponse response = aiClient.estimateThetaBatch(
                    new EstimateThetaBatchRequest(payload));

            if (response != null && response.results() != null) {
                for (EstimateThetaBatchResponse.Item item : response.results()) {
                    result.put(item.key(), new EstimateThetaResponse(item.theta(), item.thetaScaled(), item.thetaVariance()));
                }
            }
        } catch (RuntimeException ex) {
            log.warn("AI batch theta service unavailable, using local IRT fallback: {}", ex.getMessage());
        }

        // Điền thiếu bằng tính toán nội bộ
        for (Map.Entry<String, EstimateThetaRequest> entry : items.entrySet()) {
            result.computeIfAbsent(entry.getKey(), key -> {
                EstimateThetaRequest req = entry.getValue();
                return computeThetaLocally(
                        req.oldTheta(),
                        req.oldThetaVar(),
                        req.responses(),
                        req.difficulties(),
                        req.weights());
            });
        }

        return result;
    }

    /**
     * Sinh embedding cho danh sách văn bản (dùng cho index lý thuyết và cho
     * câu hỏi của sinh viên trước khi tìm kiếm pgvector).
     *
     * @return danh sách vector theo đúng thứ tự {@code texts}, hoặc
     *         {@code null} nếu AI Service lỗi/timeout — caller phải tự xử lý
     *         (không có fallback embedding nội bộ khả thi).
     */
    public List<float[]> embedTexts(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }
        try {
            EmbedResponse response = aiClient.embed(new EmbedRequest(texts));
            if (response == null || response.embeddings() == null) {
                return null;
            }
            List<float[]> vectors = new ArrayList<>(response.embeddings().size());
            for (List<Float> embedding : response.embeddings()) {
                float[] vector = new float[embedding.size()];
                for (int i = 0; i < embedding.size(); i++) {
                    vector[i] = embedding.get(i);
                }
                vectors.add(vector);
            }
            return vectors;
        } catch (RuntimeException ex) {
            log.warn("AI embedding service unavailable: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Sinh câu trả lời RAG cho chatbox lý thuyết. Không bao giờ throw ra
     * ngoài — chat luôn phải trả về một câu trả lời (kể cả khi là câu xin
     * lỗi do AI Service lỗi), để trải nghiệm chat không bị vỡ giữa chừng.
     */
    public String chat(ChatRequest request) {
        try {
            ChatResponse response = aiClient.chat(request);
            if (response != null && response.answer() != null && !response.answer().isBlank()) {
                return response.answer();
            }
        } catch (RuntimeException ex) {
            log.warn("AI chat service unavailable: {}", ex.getMessage());
        }
        return CHAT_FALLBACK_MESSAGE;
    }

    /**
     * Sinh lời khuyên cải thiện dựa trên delta theta từng kỹ năng sau khi
     * nộp bài. Nếu AI Service lỗi, fallback sang template rule-based (không
     * cần LLM) để người dùng luôn nhận được lời khuyên nào đó.
     */
    public String generateAdvice(List<SkillDeltaItem> skillDeltas) {
        if (skillDeltas == null || skillDeltas.isEmpty()) {
            return "Bài làm này chưa ghi nhận kỹ năng nào để đưa ra lời khuyên.";
        }
        try {
            var response = aiClient.generateAdvice(new GenerateAdviceRequest(skillDeltas));
            if (response != null && response.advice() != null && !response.advice().isBlank()) {
                return response.advice();
            }
        } catch (RuntimeException ex) {
            log.warn("AI advice service unavailable, using rule-based fallback: {}", ex.getMessage());
        }
        return generateAdviceLocally(skillDeltas);
    }

    /**
     * Lời khuyên rule-based khi Gemini không khả dụng: chỉ ra tối đa 3 kỹ
     * năng giảm nhiều nhất cần ôn lại, ghi nhận kỹ năng tăng nhiều nhất nếu
     * có, không dùng LLM.
     */
    private String generateAdviceLocally(List<SkillDeltaItem> skillDeltas) {
        List<SkillDeltaItem> declined = skillDeltas.stream()
                .filter(item -> item.delta() < 0)
                .sorted(Comparator.comparingDouble(SkillDeltaItem::delta))
                .limit(3)
                .toList();

        SkillDeltaItem mostImproved = skillDeltas.stream()
                .max(Comparator.comparingDouble(SkillDeltaItem::delta))
                .filter(item -> item.delta() > 0.05)
                .orElse(null);

        StringBuilder advice = new StringBuilder();
        if (!declined.isEmpty()) {
            advice.append("Bạn nên ưu tiên ôn lại: ");
            advice.append(String.join(", ", declined.stream().map(SkillDeltaItem::skillName).toList()));
            advice.append(" — năng lực các kỹ năng này đang giảm so với trước. ");
        } else {
            advice.append("Năng lực các kỹ năng của bạn không giảm ở lần làm bài này, hãy tiếp tục duy trì. ");
        }

        if (mostImproved != null) {
            advice.append("Kỹ năng \"").append(mostImproved.skillName())
                    .append("\" có tiến bộ rõ rệt, tiếp tục phát huy nhé!");
        }

        return advice.toString();
    }

    /**
     * Bản Java của {@code AI/main.py -> rasch_theta}. Giữ nguyên trình tự
     * Newton-Raphson: tính gradient/hessian, cập nhật theta, clamp về
     * {@code [-4, 4]} rồi kiểm tra hội tụ trên {@code |new_theta - theta|}.
     */

    EstimateThetaResponse computeThetaLocally(double oldTheta, double oldThetaVar, List<Integer> responses, List<Double> difficulties, List<Double> weights) {

        int itemCount = usableItemCount(responses, difficulties, weights);
        double priorVar = oldThetaVar + 0.05;
        if (itemCount == 0) {
            return new EstimateThetaResponse(oldTheta, ThetaUtils.toScaled(oldTheta), priorVar);
        }

        double theta = oldTheta;
        double hessian = -1.0 / priorVar;

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            double gradient = 0.0;
            hessian = 0.0;

            for (int index = 0; index < itemCount; index++) {
                int response = sanitizeResponse(responses.get(index));
                double difficulty = finiteOrDefault(difficulties.get(index), DEFAULT_THETA);
                double weight = positiveOrDefault(weights.get(index), 1.0);
                double probability = probability(theta, difficulty);

                gradient += weight * (response - probability);
                hessian -= weight * probability * (1.0 - probability);
            }

            gradient -= (theta - oldTheta) / priorVar;
            hessian -= 1.0 / priorVar;

            if (hessian == 0) break;

            double newTheta = theta - gradient / hessian;
            newTheta = Math.max(THETA_MIN, Math.min(THETA_MAX, newTheta));

            if (Math.abs(newTheta - theta) < CONVERGENCE_THRESHOLD) {
                theta = newTheta;
                break;
            }

            theta = newTheta;
        }

        double thetaVariance = hessian != 0 ? -1.0 / hessian : priorVar;
        thetaVariance = Math.max(1e-4, Math.min(4.0, thetaVariance));

        return new EstimateThetaResponse(theta, ThetaUtils.toScaled(theta), thetaVariance);
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
        return new EstimateThetaResponse(theta, ThetaUtils.toScaled(theta), 1.0);
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

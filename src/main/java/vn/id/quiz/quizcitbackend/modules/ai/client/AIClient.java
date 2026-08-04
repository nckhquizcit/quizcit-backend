package vn.id.quiz.quizcitbackend.modules.ai.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import vn.id.quiz.quizcitbackend.modules.ai.dto.ChatRequest;
import vn.id.quiz.quizcitbackend.modules.ai.dto.ChatResponse;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EmbedRequest;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EmbedResponse;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaBatchRequest;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaBatchResponse;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaRequest;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaResponse;
import vn.id.quiz.quizcitbackend.modules.ai.dto.GenerateAdviceRequest;
import vn.id.quiz.quizcitbackend.modules.ai.dto.GenerateAdviceResponse;


/**
 * Client gọi sang AI Service (IRT estimation + chatbox RAG + lời khuyên).
 *
 * <p><b>Fix Bug 5 (Phase 1 - P2):</b> Trước đây {@code post().uri(...)}
 * truyền thẳng URL {@code https://quizcit-ai.onrender.com} (không có
 * path) khiến AI Service trả về 404. Sau khi fix:
 * <ul>
 *   <li>Path chuẩn được tách riêng thành hằng số {@link #ESTIMATE_PATH}
 *       để dễ thay đổi.</li>
 *   <li>URL truyền vào {@code uri()} chỉ là path, còn host được cấu hình
 *       qua {@code AIConfig#aiRestClient} (đọc từ property).</li>
 *   <li>Gửi kèm Request Body {@link EstimateThetaRequest} thực tế,
 *       không hard-code như phiên bản cũ.</li>
 * </ul>
 * </p>
 *
 * <p>Các endpoint dùng Gemini ({@code /embed}, {@code /chat}, {@code /advice})
 * dùng {@code aiChatRestClient} (timeout dài hơn) thay vì {@code aiRestClient}
 * (timeout rất ngắn, dành cho fallback nhanh của Rasch).</p>
 */
@Component
public class AIClient {

    // Khớp với endpoint thực tế của AI Service (AI/main.py -> @app.post("/estimate")).
    private static final String ESTIMATE_PATH = "/estimate";
    // Endpoint batch: ước lượng theta cho nhiều skill trong 1 request
    // (AI/main.py -> @app.post("/estimate-batch")).
    private static final String ESTIMATE_BATCH_PATH = "/estimate-batch";
    private static final String EMBED_PATH = "/embed";
    private static final String CHAT_PATH = "/chat";
    private static final String ADVICE_PATH = "/advice";


    private final RestClient aiRestClient;

    @Qualifier("aiChatRestClient")
    private final RestClient aiChatRestClient;

    @Autowired
    public AIClient(
            @Qualifier("aiRestClient") RestClient aiRestClient,
            @Qualifier("aiChatRestClient") RestClient aiChatRestClient) {
        this.aiRestClient = aiRestClient;
        this.aiChatRestClient = aiChatRestClient;
    }

    /** Giữ tương thích với các caller cũ chỉ truyền một RestClient. */
    public AIClient(RestClient aiRestClient) {
        this(aiRestClient, aiRestClient);
    }

    public EstimateThetaResponse estimateTheta(EstimateThetaRequest request) {
        return aiRestClient.post()
                .uri(ESTIMATE_PATH)
                .body(request)
                .retrieve()
                .body(EstimateThetaResponse.class);
    }

    public EstimateThetaBatchResponse estimateThetaBatch(EstimateThetaBatchRequest request) {
        return aiRestClient.post()
                .uri(ESTIMATE_BATCH_PATH)
                .body(request)
                .retrieve()
                .body(EstimateThetaBatchResponse.class);
    }

    public EmbedResponse embed(EmbedRequest request) {
        return aiChatRestClient.post()
                .uri(EMBED_PATH)
                .body(request)
                .retrieve()
                .body(EmbedResponse.class);
    }

    public ChatResponse chat(ChatRequest request) {
        return aiChatRestClient.post()
                .uri(CHAT_PATH)
                .body(request)
                .retrieve()
                .body(ChatResponse.class);
    }

    public GenerateAdviceResponse generateAdvice(GenerateAdviceRequest request) {
        return aiChatRestClient.post()
                .uri(ADVICE_PATH)
                .body(request)
                .retrieve()
                .body(GenerateAdviceResponse.class);
    }
}

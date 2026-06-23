package vn.id.quiz.quizcitbackend.modules.ai.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaRequest;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaResponse;

/**
 * Client gọi sang AI Service (IRT estimation).
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
 */
@Component
@RequiredArgsConstructor
public class AIClient {

    // Khớp với endpoint thực tế của AI Service (AI/main.py -> @app.post("/estimate")).
    private static final String ESTIMATE_PATH = "/estimate";


    private final RestClient aiRestClient;

    public EstimateThetaResponse estimateTheta(EstimateThetaRequest request) {
        return aiRestClient.post()
                .uri(ESTIMATE_PATH)
                .body(request)
                .retrieve()
                .body(EstimateThetaResponse.class);
    }
}

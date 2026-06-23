package vn.id.quiz.quizcitbackend.modules.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Cấu hình RestClient cho AI Service.
 *
 * <p><b>Fix Bug 5 (Phase 1 - P2):</b> Trước đây {@code baseUrl} được hard
 * code là {@code http://localhost:8000} trong khi {@code AIClient} gọi tới
 * {@code https://quizcit-ai.onrender.com} (không có path), gây ra lỗi 404
 * khi deploy thực tế. Sau khi fix:
 * <ul>
 *   <li>Đọc URL từ property {@code quizcit.ai.base-url} với giá trị mặc định
 *       an toàn là {@code http://localhost:8000} cho môi trường dev.</li>
 *   <li>Endpoint IRT được chuẩn hóa thành {@code /api/v1/ai/estimate-theta}
 *       (xem {@link vn.id.quiz.quizcitbackend.modules.ai.client.AIClient}).</li>
 * </ul>
 * </p>
 */
@Configuration
public class AIConfig {

    @Bean
    public RestClient aiRestClient(
            @Value("${quizcit.ai.base-url:http://localhost:8000}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}

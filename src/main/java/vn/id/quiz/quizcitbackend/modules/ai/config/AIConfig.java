package vn.id.quiz.quizcitbackend.modules.ai.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
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
 *
 * <p><b>Fix hiệu năng nộp bài:</b> AI Service host trên nền tảng có thể
 * "ngủ đông" (cold start 20-50s). Trước đây {@code aiRestClient} KHÔNG có
 * timeout nên khi nộp bài, backend chờ AI Service thức dậy tới hàng chục
 * giây thay vì fallback sang thuật toán IRT nội bộ. Thêm connect/read
 * timeout để {@code AIService} chuyển sang fallback gần như tức thì khi
 * AI Service không phản hồi kịp.</p>
 */
@Configuration
public class AIConfig {

    @Bean
    public RestClient aiRestClient(
            @Value("${quizcit.ai.base-url:http://localhost:8000}") String baseUrl,
            @Value("${quizcit.ai.connect-timeout-ms:500}") long connectTimeoutMs,
            @Value("${quizcit.ai.read-timeout-ms:750}") long readTimeoutMs) {

        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofMillis(connectTimeoutMs))
                .withReadTimeout(Duration.ofMillis(readTimeoutMs));

        ClientHttpRequestFactory requestFactory =
                ClientHttpRequestFactories.get(settings);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}

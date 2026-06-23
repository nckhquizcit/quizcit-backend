package vn.id.quiz.quizcitbackend.modules.ai.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.ai.client.AIClient;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaRequest;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaResponse;

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
public class AIService {

    private final AIClient aiClient;

    public EstimateThetaResponse estimateTheta(EstimateThetaRequest request) {
        return aiClient.estimateTheta(request);
    }
}

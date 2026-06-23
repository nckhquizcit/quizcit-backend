package vn.id.quiz.quizcitbackend.modules.ai.dto;

/**
 * Response từ AI Service.
 *
 * @param theta       năng lực ước lượng (theta) của người dùng
 * @param standardError sai số chuẩn của ước lượng (nếu có)
 */
public record EstimateThetaResponse(
        double theta,
        Double standardError
) {
}

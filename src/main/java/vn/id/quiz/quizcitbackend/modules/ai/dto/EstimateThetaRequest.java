package vn.id.quiz.quizcitbackend.modules.ai.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Request gọi AI Service ước lượng theta.
 *
 * <p>Field {@code weights} phải khớp với contract của AI Service
 * ({@code AI/main.py} -> {@code AttemptData.weights}). Trọng số lấy
 * từ {@code question_skills.weight} của từng câu hỏi.</p>
 *
 * @param oldTheta    theta (năng lực) cũ trước khi làm bài (prior mean)
 * @param responses   danh sách đáp án đúng (1) / sai (0)
 * @param difficulties danh sách độ khó (b) tương ứng từng câu
 * @param weights     danh sách trọng số (question_skills.weight) tương ứng từng câu
 */
public record EstimateThetaRequest(
        double oldTheta,
        double oldThetaVar,
        @NotNull @NotEmpty List<Integer> responses,
        @NotNull @NotEmpty List<Double> difficulties,
        @NotNull @NotEmpty List<Double> weights
) {
}


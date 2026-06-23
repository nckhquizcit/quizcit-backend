package vn.id.quiz.quizcitbackend.modules.ai.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Request gọi AI Service ước lượng theta.
 *
 * @param responses   danh sách đáp án đúng (1) / sai (0)
 * @param difficulties danh sách độ khó (b) tương ứng từng câu
 * @param discriminations danh sách độ phân biệt (a) tương ứng từng câu
 */
public record EstimateThetaRequest(
        @NotNull @NotEmpty List<Integer> responses,
        @NotNull @NotEmpty List<Double> difficulties,
        @NotNull @NotEmpty List<Double> discriminations
) {
}

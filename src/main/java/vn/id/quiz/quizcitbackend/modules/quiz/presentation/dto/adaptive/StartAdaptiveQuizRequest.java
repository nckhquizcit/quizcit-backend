package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.adaptive;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartAdaptiveQuizRequest {
    @NotNull(message = "totalQuestion không được để trống")
    @Min(value = 15, message = "Bài thích ứng phải có ít nhất 15 câu")
    private Integer totalQuestion;
}

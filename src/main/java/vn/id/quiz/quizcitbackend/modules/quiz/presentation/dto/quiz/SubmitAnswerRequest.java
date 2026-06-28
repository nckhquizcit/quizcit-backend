package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitAnswerRequest {

    @NotBlank(message = "questionId không được để trống")
    private String questionId;

    private String optionId;
}

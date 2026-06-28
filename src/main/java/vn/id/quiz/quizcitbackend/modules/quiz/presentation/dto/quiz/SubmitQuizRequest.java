package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitQuizRequest {

    private String quizId;

    @NotBlank(message = "attemptId không được để trống")
    private String attemptId;

    @Valid
    @NotNull(message = "answers không được để trống")
    private List<SubmitAnswerRequest> answers;
}
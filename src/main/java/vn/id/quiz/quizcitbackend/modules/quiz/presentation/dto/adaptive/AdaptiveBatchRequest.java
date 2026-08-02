package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.adaptive;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.SubmitAnswerRequest;

@Getter
@Setter
public class AdaptiveBatchRequest {
    @Valid
    @NotNull(message = "answers không được để trống")
    private List<SubmitAnswerRequest> answers;
}

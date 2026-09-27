package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz;

import java.util.Map;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveAttemptDraftRequest {
    @NotNull
    private Map<String, String> answers;
}

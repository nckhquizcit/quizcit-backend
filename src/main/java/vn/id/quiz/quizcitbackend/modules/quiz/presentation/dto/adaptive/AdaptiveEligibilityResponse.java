package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.adaptive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdaptiveEligibilityResponse {
    private String subjectId;
    private int requiredQuizzes;
    private int completedQuizzes;
    private int remainingQuizzes;
    private boolean eligible;
}

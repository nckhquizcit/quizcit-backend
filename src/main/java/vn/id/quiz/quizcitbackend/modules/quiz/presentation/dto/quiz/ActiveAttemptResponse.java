package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz;

import java.time.ZonedDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActiveAttemptResponse {
    private String attemptId;
    private String quizId;
    private ZonedDateTime deadlineAt;
}

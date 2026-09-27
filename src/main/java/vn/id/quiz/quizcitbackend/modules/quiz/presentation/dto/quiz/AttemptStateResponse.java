package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz;

import java.time.ZonedDateTime;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.AttemptStatus;

@Getter
@Builder
public class AttemptStateResponse {
    private String attemptId;
    private String quizId;
    private int totalQuestion;
    private AttemptStatus status;
    private ZonedDateTime deadlineAt;
    private Map<String, String> answers;
}

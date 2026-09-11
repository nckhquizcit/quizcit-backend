package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.adaptive;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.QuestionResponse;

@Getter
@Builder
public class AdaptiveQuizResponse {
    private String quizId;
    private String attemptId;
    private String quizName;
    private String subjectName;
    private int totalQuestion;
    private int answeredQuestion;
    private int page;
    private boolean completed;
    private List<QuestionResponse> questions;
}

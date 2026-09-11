package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz;

import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.QuestionResponse;
import java.util.List;

public record QuizDetailWithQuestionsResponse(
        QuizResponse quiz,
        List<QuestionResponse> questions
) {
    public QuizDetailWithQuestionsResponse {
        if (questions == null) {
            questions = List.of();
        }
    }
}

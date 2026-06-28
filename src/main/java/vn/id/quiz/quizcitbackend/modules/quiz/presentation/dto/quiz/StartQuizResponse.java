package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StartQuizResponse {

    private String attemptId;

    private String quizId;

    private int totalQuestion;
}
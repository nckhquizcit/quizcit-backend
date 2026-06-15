package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizResultResponse {

    private String attemptId;

    private String quizId;

    private double rawScore;

    private int totalQuestion;

    private double thetaBefore;

    private double thetaAfter;

    private double thetaSmoothed;

    private double standardError;
}
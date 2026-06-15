package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizResultResponse {

    private String attemptId;

    private String quizId;

    private double rawScore;

    private int totalQuestion;

    private double overallTheta;
    
    private List<SkillResultResponse> skillResults;
}
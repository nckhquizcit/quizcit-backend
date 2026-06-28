package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.skill.SkillResultResponse;

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
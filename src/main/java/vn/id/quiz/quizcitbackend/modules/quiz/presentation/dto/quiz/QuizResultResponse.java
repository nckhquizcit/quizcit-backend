package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz;

import java.time.ZonedDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.skill.SkillResultResponse;

@Getter
@Builder
public class QuizResultResponse {

    private String attemptId;

    private String quizId;

    private String quizName;

    private String subjectName;

    private ZonedDateTime submittedAt;

    private double rawScore;

    private int totalQuestion;

    private double overallTheta;

    private List<SkillResultResponse> skillResults;

    private List<QuestionResultResponse> questionResults;

    // MỚI: lời khuyên cải thiện do AI sinh dựa trên delta theta từng skill,
    // được lưu 1 lần tại thời điểm nộp bài (xem Attempt.adviceText).
    private String overallAdvice;
}

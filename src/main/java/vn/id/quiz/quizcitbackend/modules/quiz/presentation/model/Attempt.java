package vn.id.quiz.quizcitbackend.modules.quiz.presentation.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;

@Entity
@Table(name = "attempts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Attempt {

    @Id
    @Column(name = "attempt_id", length = 50)
    private String attemptId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "quiz_id", nullable = false, length = 50)
    private String quizId;

    @Column(name = "raw_score")
    private double rawScore;

    @Column(name = "total_question")
    private int totalQuestion;

    @Column(name = "theta_before")
    private double thetaBefore;

    @Column(name = "theta_after")
    private double thetaAfter;

    @Column(name = "theta_smoothed")
    private double thetaSmoothed;

    @Column(name = "standard_error")
    private double standardError;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    public static Attempt create(
            String userId,
            String quizId,
            int totalQuestion,
            double thetaBefore) {
        return Attempt.builder()
                .attemptId(UUID.randomUUID().toString())
                .userId(userId)
                .quizId(quizId)
                .totalQuestion(totalQuestion)
                .thetaBefore(thetaBefore)
                .createdAt(ZonedDateTime.now())
                .build();
    }

    public void complete(
            double rawScore,
            double thetaAfter,
            double thetaSmoothed,
            double standardError) {
        this.rawScore = rawScore;
        this.thetaAfter = thetaAfter;
        this.thetaSmoothed = thetaSmoothed;
        this.standardError = standardError;
    }
}
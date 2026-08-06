package vn.id.quiz.quizcitbackend.modules.quiz.presentation.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "adaptive_question_selections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AdaptiveQuestionSelection {

    @Id
    @Column(name = "selection_id", length = 50)
    private String selectionId;

    @Column(name = "attempt_id", nullable = false, length = 50)
    private String attemptId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "question_id", nullable = false, length = 50)
    private String questionId;

    @Column(name = "skill_id", nullable = false, length = 50)
    private String skillId;

    @Column(name = "theta_at_selection", nullable = false)
    private double thetaAtSelection;

    @Column(name = "difficulty_at_selection", nullable = false)
    private double difficultyAtSelection;

    @Column(name = "absolute_difference", nullable = false)
    private double absoluteDifference;

    @Column(name = "is_primary_skill", nullable = false)
    private boolean primarySkill;

    @Column(name = "is_driving_skill", nullable = false)
    private boolean drivingSkill;

    @Column(name = "batch_number", nullable = false)
    private int batchNumber;

    @Column(name = "question_order", nullable = false)
    private int questionOrder;

    @Column(name = "selected_at", nullable = false)
    private ZonedDateTime selectedAt;

    public static AdaptiveQuestionSelection create(
            String attemptId,
            String userId,
            String questionId,
            String skillId,
            double thetaAtSelection,
            double difficultyAtSelection,
            boolean primarySkill,
            boolean drivingSkill,
            int batchNumber,
            int questionOrder,
            ZonedDateTime selectedAt) {
        return AdaptiveQuestionSelection.builder()
                .selectionId(UUID.randomUUID().toString())
                .attemptId(attemptId)
                .userId(userId)
                .questionId(questionId)
                .skillId(skillId)
                .thetaAtSelection(thetaAtSelection)
                .difficultyAtSelection(difficultyAtSelection)
                .absoluteDifference(Math.abs(thetaAtSelection - difficultyAtSelection))
                .primarySkill(primarySkill)
                .drivingSkill(drivingSkill)
                .batchNumber(batchNumber)
                .questionOrder(questionOrder)
                .selectedAt(selectedAt)
                .build();
    }
}

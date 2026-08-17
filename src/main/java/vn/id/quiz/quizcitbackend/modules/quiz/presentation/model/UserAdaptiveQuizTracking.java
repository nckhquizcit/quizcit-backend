package vn.id.quiz.quizcitbackend.modules.quiz.presentation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(name = "user_adaptive_quiz_trackings")
@IdClass(UserAdaptiveQuizTrackingId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserAdaptiveQuizTracking {
    public static final int DEFAULT_REQUIRED_STANDARD_QUIZZES = 2;

    @Id
    @Column(name = "user_id", length = 50, nullable = false)
    private String userId;

    @Id
    @Column(name = "subject_id", length = 50, nullable = false)
    private String subjectId;

    @Column(name = "completed_standard_quizzes", nullable = false)
    @Builder.Default
    private int completedStandardQuizzes = 0;

    @Column(name = "required_standard_quizzes", nullable = false)
    @Builder.Default
    private int requiredStandardQuizzes = DEFAULT_REQUIRED_STANDARD_QUIZZES;

    @Column(name = "last_adaptive_completed_at")
    private ZonedDateTime lastAdaptiveCompletedAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    public static UserAdaptiveQuizTracking create(String userId, String subjectId) {
        return UserAdaptiveQuizTracking.builder()
                .userId(userId)
                .subjectId(subjectId)
                .completedStandardQuizzes(0)
                .requiredStandardQuizzes(DEFAULT_REQUIRED_STANDARD_QUIZZES)
                .updatedAt(ZonedDateTime.now())
                .build();
    }

    public void incrementStandardQuizzes() {
        this.completedStandardQuizzes++;
        this.updatedAt = ZonedDateTime.now();
    }

    public void resetForNewAdaptiveQuiz() {
        this.completedStandardQuizzes = 0;
        this.lastAdaptiveCompletedAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
    }

    public boolean isEligible() {
        return this.completedStandardQuizzes >= this.requiredStandardQuizzes;
    }

    public int getRemainingQuizzes() {
        return Math.max(0, this.requiredStandardQuizzes - this.completedStandardQuizzes);
    }
}

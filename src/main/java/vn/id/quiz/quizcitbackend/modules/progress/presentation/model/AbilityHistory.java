package vn.id.quiz.quizcitbackend.modules.progress.presentation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ability_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AbilityHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "skill_id", nullable = false, length = 50)
    private String skillId;

    @Column(nullable = false)
    private double theta;

    @Column(nullable = false)
    private double delta;
    
    @Column(name = "standard_error")
    private Double standardError;

    @Column(name = "attempt_id", length = 50)
    private String attemptId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static AbilityHistory create(
            String userId,
            String skillId,
            double theta,
            double delta,
            Double standardError,
            String attemptId) {
        return AbilityHistory.builder()
                .userId(userId)
                .skillId(skillId)
                .theta(theta)
                .delta(delta)
                .standardError(standardError)
                .attemptId(attemptId)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
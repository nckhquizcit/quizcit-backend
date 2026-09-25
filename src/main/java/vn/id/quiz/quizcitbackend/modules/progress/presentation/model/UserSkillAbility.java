package vn.id.quiz.quizcitbackend.modules.progress.presentation.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "user_skill_ability",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "skill_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserSkillAbility {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "skill_id", length = 50)
    private String skillId;

    private Double theta;

    @Column(name = "theta_variance")
    private Double thetaVariance;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    public static UserSkillAbility create(
            String userId,
            String skillId) {
        return UserSkillAbility.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .skillId(skillId)
                .theta(0.0)
                .thetaVariance(1.0)
                .updatedAt(ZonedDateTime.now())
                .build();
    }

    public void updateAbility(
            double theta,
            double thetaVariance) {
        this.theta = theta;
        this.thetaVariance = thetaVariance;
        this.updatedAt = ZonedDateTime.now();
    }
}

package vn.id.quiz.quizcitbackend.modules.progress.presentation.model;

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
@Table(name = "user_skill_ability")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserSkillAbility {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "skill_id", nullable = false, length = 50)
    private String skillId;

    private double theta;

    @Column(name = "standard_error")
    private double standardError;

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
                .standardError(1.0)
                .updatedAt(ZonedDateTime.now())
                .build();
    }

    public void updateAbility(
            double theta,
            double standardError) {
        this.theta = theta;
        this.standardError = standardError;
        this.updatedAt = ZonedDateTime.now();
    }
}
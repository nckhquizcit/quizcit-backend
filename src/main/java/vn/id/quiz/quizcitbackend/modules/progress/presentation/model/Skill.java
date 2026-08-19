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
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "skills")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
public class Skill {

    @Id
    @Column(name = "skill_id", length = 50)
    private String skillId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;

    public static Skill create(
            String name,
            String description) {
        return Skill.builder()
                .skillId(UUID.randomUUID().toString())
                .name(name)
                .description(description)
                .createdAt(ZonedDateTime.now())
                .build();
    }

    public void updateSkill(
            String name,
            String description) {
        this.name = name;
        this.description = description;
    }

    public void softDelete() {
        this.deletedAt = ZonedDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
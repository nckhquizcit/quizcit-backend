package vn.id.quiz.quizcitbackend.modules.progress.presentation.model;

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
@Table(name = "skills")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Skill {

    @Id
    @Column(name = "skill_id", length = 50)
    private String skillId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    public static Skill create(
            String name,
            String description) {
        return Skill.builder()
                .skillId(UUID.randomUUID().toString())
                .name(name)
                .description(description)
                .build();
    }

    public void updateSkill(
            String name,
            String description) {
        this.name = name;
        this.description = description;
    }
}
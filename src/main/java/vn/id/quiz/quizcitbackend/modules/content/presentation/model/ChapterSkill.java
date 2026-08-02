package vn.id.quiz.quizcitbackend.modules.content.presentation.model;

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

@Entity
@Table(name = "chapter_skills")
@IdClass(ChapterSkillId.class)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChapterSkill {
    @Id @Column(name = "chapter_id", length = 50)
    private String chapterId;
    @Id @Column(name = "skill_id", length = 50)
    private String skillId;
    @Column(name = "required_theta", nullable = false)
    private double requiredTheta;

    public static ChapterSkill create(String chapterId, String skillId, double requiredTheta) {
        return ChapterSkill.builder().chapterId(chapterId).skillId(skillId)
                .requiredTheta(requiredTheta).build();
    }
}

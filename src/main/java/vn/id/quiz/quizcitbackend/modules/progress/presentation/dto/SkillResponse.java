package vn.id.quiz.quizcitbackend.modules.progress.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.Skill;

/**
 * Response trả về thông tin một {@link Skill}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillResponse {

    private String skillId;
    private String name;
    private String description;

    public static SkillResponse from(Skill skill) {
        if (skill == null) {
            return null;
        }
        return SkillResponse.builder()
                .skillId(skill.getSkillId())
                .name(skill.getName())
                .description(skill.getDescription())
                .build();
    }
}

package vn.id.quiz.quizcitbackend.modules.progress.presentation.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserSkillAbilityTest {

    @Test
    void createShouldMatchUserSkillAbilityDefaults() {
        UserSkillAbility ability = UserSkillAbility.create("user-1", "skill-1");

        assertThat(ability.getId()).isNotBlank();
        assertThat(ability.getUserId()).isEqualTo("user-1");
        assertThat(ability.getSkillId()).isEqualTo("skill-1");
        assertThat(ability.getTheta()).isEqualTo(0.0);
        assertThat(ability.getStandardError()).isEqualTo(1.0);
        assertThat(ability.getUpdatedAt()).isNotNull();
    }
}

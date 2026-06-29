package vn.id.quiz.quizcitbackend.modules.progress.presentation.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

class AbilityHistoryTest {

    @Test
    void createShouldMatchAbilityHistoryDefaults() {
        AbilityHistory history = AbilityHistory.create(
                "user-1",
                "skill-1",
                1.2,
                0.3,
                "attempt-1");

        assertThat(history.getHistoryId()).isNotBlank();
        assertThat(history.getUserId()).isEqualTo("user-1");
        assertThat(history.getSkillId()).isEqualTo("skill-1");
        assertThat(history.getTheta()).isEqualTo(1.2);
        assertThat(history.getDelta()).isEqualTo(0.3);
        assertThat(history.getAttemptId()).isEqualTo("attempt-1");
        assertThat(history.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldNotExposeStandardErrorField() {
        assertThat(AbilityHistory.class.getDeclaredFields())
                .extracting(Field::getName)
                .doesNotContain("standardError");
    }
}

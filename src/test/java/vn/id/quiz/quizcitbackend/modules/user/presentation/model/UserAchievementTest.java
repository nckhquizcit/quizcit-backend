package vn.id.quiz.quizcitbackend.modules.user.presentation.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserAchievementTest {

    @Test
    void createShouldMatchUserAchievementDefaults() {
        UserAchievement achievement = UserAchievement.create(
                "user-1",
                "achievement-1",
                "First badge",
                "Complete the first quiz",
                "QUIZ_COUNT",
                1
        );

        assertThat(achievement.getUserId()).isEqualTo("user-1");
        assertThat(achievement.getAchieveId()).isEqualTo("achievement-1");
        assertThat(achievement.getAchieved()).isFalse();
        assertThat(achievement.getBadge()).isEqualTo("First badge");
        assertThat(achievement.getDescription()).isEqualTo("Complete the first quiz");
        assertThat(achievement.getConditionType()).isEqualTo("QUIZ_COUNT");
        assertThat(achievement.getConditionValue()).isEqualTo(1);
        assertThat(achievement.getAchievedAt()).isNull();
    }

    @Test
    void markAchievedShouldSetFlagAndTimestamp() {
        UserAchievement achievement = UserAchievement.create(
                "user-1",
                "achievement-1",
                "First badge",
                "Complete the first quiz",
                "QUIZ_COUNT",
                1
        );

        achievement.markAchieved();

        assertThat(achievement.getAchieved()).isTrue();
        assertThat(achievement.getAchievedAt()).isNotNull();
    }
}

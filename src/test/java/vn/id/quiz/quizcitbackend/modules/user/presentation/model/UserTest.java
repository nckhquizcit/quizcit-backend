package vn.id.quiz.quizcitbackend.modules.user.presentation.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void createShouldMatchUsersTableDefaults() {
        User user = User.create("  student01  ", "secret123", " student01@example.com ", null);

        assertThat(user.getUserId()).isNotBlank();
        assertThat(user.getUsername()).isEqualTo("student01");
        assertThat(user.getPassword()).isEqualTo("secret123");
        assertThat(user.getEmail()).isEqualTo("student01@example.com");
        assertThat(user.getDisplayName()).isEqualTo("student01");
        assertThat(user.getStatus()).isEqualTo(UserStatus.UNVERIFIED);
        assertThat(user.getCreatedAt()).isNotNull();
    }
}

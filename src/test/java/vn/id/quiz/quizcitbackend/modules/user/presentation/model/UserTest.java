package vn.id.quiz.quizcitbackend.modules.user.presentation.model;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @Test
    void createShouldRejectBlankPassword() {
        assertThatThrownBy(() -> User.create("student01", " ", "student01@example.com", "Student"))
                .isInstanceOf(AppException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void changePasswordShouldReplaceStoredPassword() {
        User user = User.create("student01", "secret123", "student01@example.com", "Student");

        user.changePassword("newSecret123");

        assertThat(user.getPassword()).isEqualTo("newSecret123");
        assertThat(user.getUpdatedAt()).isAfterOrEqualTo(user.getCreatedAt());
    }
}

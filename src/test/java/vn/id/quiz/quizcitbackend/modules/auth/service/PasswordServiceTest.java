package vn.id.quiz.quizcitbackend.modules.auth.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordServiceTest {

    private final PasswordService passwordService = new PasswordService();

    @Test
    void hashShouldStorePasswordAsPbkdf2AndVerifyRawPassword() {
        String hashedPassword = passwordService.hash("secret123");

        assertThat(hashedPassword).startsWith("pbkdf2$");
        assertThat(hashedPassword).isNotEqualTo("secret123");
        assertThat(passwordService.matches("secret123", hashedPassword)).isTrue();
        assertThat(passwordService.matches("wrong-password", hashedPassword)).isFalse();
    }

    @Test
    void matchesShouldAcceptLegacyPlainPasswordForExistingDatabaseRows() {
        assertThat(passwordService.matches("secret123", "secret123")).isTrue();
        assertThat(passwordService.matches("wrong-password", "secret123")).isFalse();
    }
}

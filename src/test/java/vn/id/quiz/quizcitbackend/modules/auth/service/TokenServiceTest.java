package vn.id.quiz.quizcitbackend.modules.auth.service;

import org.junit.jupiter.api.Test;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;
import vn.id.quiz.quizcitbackend.shared.security.TokenPayload;

import static org.assertj.core.api.Assertions.assertThat;

class TokenServiceTest {

    private final TokenService tokenService = new TokenService(
            "test-secret-that-is-long-enough-for-hmac", 3600,
            "test-otp-secret-that-is-long-enough", 300);

    @Test
    void generateShouldReturnSignedBearerToken() {
        User user = User.create("student01", "secret123", "student01@example.com", "Student One");

        TokenDetails tokenDetails = tokenService.generate(user, UserRole.USER);

        assertThat(tokenDetails.accessToken()).isNotBlank();
        assertThat(tokenDetails.accessToken().split("\\.")).hasSize(3);
        assertThat(tokenDetails.tokenType()).isEqualTo("Bearer");
        assertThat(tokenDetails.expiresAt()).isAfter(tokenDetails.issuedAt());
        assertThat(tokenService.isValid(tokenDetails.accessToken())).isTrue();
        assertThat(TokenPayload.from(tokenDetails.accessToken()).tokenVersion()).isZero();
    }

    @Test
    void isValidShouldRejectTamperedToken() {
        User user = User.create("student01", "secret123", "student01@example.com", "Student One");
        String token = tokenService.generate(user, UserRole.USER).accessToken();
        String tamperedToken = token.substring(0, token.length() - 2) + "xx";

        assertThat(tokenService.isValid(tamperedToken)).isFalse();
    }
}

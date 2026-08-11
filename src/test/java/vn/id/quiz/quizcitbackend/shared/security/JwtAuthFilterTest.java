package vn.id.quiz.quizcitbackend.shared.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.id.quiz.quizcitbackend.modules.auth.repository.AuthRepository;
import vn.id.quiz.quizcitbackend.modules.auth.service.TokenService;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class JwtAuthFilterTest {
    private final TokenService tokens = new TokenService(
            "test-secret-that-is-long-enough-for-hmac", 900,
            "test-otp-secret-that-is-long-enough", 300);
    private final AuthRepository repository = Mockito.mock(AuthRepository.class);
    private final JwtAuthFilter filter = new JwtAuthFilter(tokens, repository);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void matchingTokenVersionShouldAuthenticate() throws Exception {
        User user = verifiedUser();
        when(repository.findUserById(user.getUserId())).thenReturn(Optional.of(user));
        String token = tokens.generate(user, UserRole.USER).accessToken();

        filter.doFilter(request(token), new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    void revokedTokenVersionShouldRemainUnauthenticated() throws Exception {
        User user = verifiedUser();
        String token = tokens.generate(user, UserRole.USER).accessToken();
        user.revokeSessions();
        when(repository.findUserById(user.getUserId())).thenReturn(Optional.of(user));

        filter.doFilter(request(token), new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private User verifiedUser() {
        User user = User.create("student", "hash", "student@example.com", "Student");
        user.setStatus(UserStatus.VERIFIED);
        return user;
    }

    private MockHttpServletRequest request(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }
}

package vn.id.quiz.quizcitbackend.modules.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.AuthResponse;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.LoginRequest;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.RegisterRequest;
import vn.id.quiz.quizcitbackend.modules.auth.repository.AuthRepository;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.Role;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRoleAssignment;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock
    private AuthRepository authRepository;

    private final AuthMapper authMapper = new AuthMapper() {
    };
    private final PasswordService passwordService = new PasswordService();
    private final TokenService tokenService = new TokenService("test-secret-that-is-long-enough-for-hmac", 3600);

    @Test
    void registerShouldHashPasswordAssignDefaultRoleAndReturnToken() {
        Role defaultRole = Role.create(UserRole.USER);
        RegisterRequest request = new RegisterRequest(
                "student01",
                "secret123",
                "student01@example.com",
                "Student One"
        );

        when(authRepository.existsByUsername(request.username())).thenReturn(false);
        when(authRepository.existsByEmail(request.email())).thenReturn(false);
        when(authRepository.findRoleByName(UserRole.USER)).thenReturn(Optional.of(defaultRole));
        when(authRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authRepository.save(any(UserRoleAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = service().register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(authRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).startsWith("pbkdf2$");
        assertThat(passwordService.matches("secret123", userCaptor.getValue().getPassword())).isTrue();

        ArgumentCaptor<UserRoleAssignment> assignmentCaptor = ArgumentCaptor.forClass(UserRoleAssignment.class);
        verify(authRepository).save(assignmentCaptor.capture());
        assertThat(assignmentCaptor.getValue().getRoleId()).isEqualTo(defaultRole.getRoleId());

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.accessToken().split("\\.")).hasSize(3);
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresAt()).isNotNull();
        assertThat(response.role()).isEqualTo(UserRole.USER);
    }

    @Test
    void loginShouldReturnTokenForValidCredentials() {
        Role role = Role.create(UserRole.USER);
        User user = User.create("student01", passwordService.hash("secret123"), "student01@example.com", "Student One");

        when(authRepository.findUserByUsername("student01")).thenReturn(Optional.of(user));
        when(authRepository.findRoleByUserId(user.getUserId())).thenReturn(Optional.of(role));

        AuthResponse response = service().login(new LoginRequest("student01", "secret123"));

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.userId()).isEqualTo(user.getUserId());
        assertThat(response.username()).isEqualTo("student01");
        assertThat(response.role()).isEqualTo(UserRole.USER);
    }

    @Test
    void loginShouldRejectWrongPassword() {
        User user = User.create("student01", passwordService.hash("secret123"), "student01@example.com", "Student One");
        when(authRepository.findUserByUsername("student01")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service().login(new LoginRequest("student01", "wrong-password")))
                .isInstanceOf(AppException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void loginShouldRejectBannedUser() {
        User user = User.create("student01", passwordService.hash("secret123"), "student01@example.com", "Student One");
        user.ban();
        when(authRepository.findUserByUsername("student01")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service().login(new LoginRequest("student01", "secret123")))
                .isInstanceOf(AppException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    private AuthServiceImpl service() {
        return new AuthServiceImpl(authRepository, authMapper, passwordService, tokenService);
    }
}

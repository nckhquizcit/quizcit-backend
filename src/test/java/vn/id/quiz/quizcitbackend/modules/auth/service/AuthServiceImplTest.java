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
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.RegisterResponse;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.model.AuthChallengePurpose;
import vn.id.quiz.quizcitbackend.modules.auth.repository.AuthRepository;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.Role;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRoleAssignment;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserStatus;
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

    @Mock private EmailOutboxService emailOutboxService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private AuthChallengeService authChallengeService;
    @Mock private AuthRateLimitService rateLimitService;
    private final AuthMapper authMapper = new AuthMapper();
    private final PasswordService passwordService = new PasswordService();
    private final TokenService tokenService = new TokenService(
            "test-secret-that-is-long-enough-for-hmac", 3600,
            "test-otp-secret-that-is-long-enough", 300);
    private final OtpService otpService = new OtpService();

    @Test
    void registerShouldHashPasswordAssignDefaultRoleAndSendOtp() {
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
        when(authRepository.findUserByEmail(request.email())).thenAnswer(invocation -> Optional.of(
                User.create(request.username(), "hash", request.email(), request.displayName())));
        when(authChallengeService.issue(any(), any(), any())).thenReturn(
                new AuthChallengeService.IssuedChallenge("challenge-id", "123456"));

        RegisterResponse response = service().register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(authRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).startsWith("pbkdf2$");
        assertThat(passwordService.matches("secret123", userCaptor.getValue().getPassword())).isTrue();
        assertThat(userCaptor.getValue().getStatus()).isEqualTo(UserStatus.UNVERIFIED);

        ArgumentCaptor<UserRoleAssignment> assignmentCaptor = ArgumentCaptor.forClass(UserRoleAssignment.class);
        verify(authRepository).save(assignmentCaptor.capture());
        assertThat(assignmentCaptor.getValue().getRoleId()).isEqualTo(defaultRole.getRoleId());

        assertThat(response.email()).isEqualTo("student01@example.com");
        assertThat(response.verificationToken()).isNotBlank();
        assertThat(response.verificationToken()).isEqualTo("challenge-id");
        verify(emailOutboxService).enqueueVerificationOtp("student01@example.com", "123456");
    }

    @Test
    void loginShouldReturnTokenForValidCredentials() {
        Role role = Role.create(UserRole.USER);
        User user = User.create("student01", passwordService.hash("secret123"), "student01@example.com", "Student One");
        user.setStatus(UserStatus.VERIFIED);

        when(authRepository.findUserByUsername("student01")).thenReturn(Optional.of(user));
        when(authRepository.findRoleByUserId(user.getUserId())).thenReturn(Optional.of(role));
        when(refreshTokenService.issue(user)).thenReturn(
                new RefreshTokenDetails("refresh-token", java.time.ZonedDateTime.now().plusDays(30)));

        AuthResponse response = service().login(new LoginRequest("student01", "secret123"));

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.userId()).isEqualTo(user.getUserId());
        assertThat(response.username()).isEqualTo("student01");
        assertThat(response.role()).isEqualTo(UserRole.USER);
    }

    @Test
    void loginShouldAcceptEmailIgnoringCaseAndSurroundingSpaces() {
        Role role = Role.create(UserRole.USER);
        User user = User.create("student01", passwordService.hash("secret123"),
                "student01@example.com", "Student One");
        user.setStatus(UserStatus.VERIFIED);

        when(authRepository.findUserByUsername("STUDENT01@EXAMPLE.COM")).thenReturn(Optional.empty());
        when(authRepository.findUserByEmail("STUDENT01@EXAMPLE.COM")).thenReturn(Optional.of(user));
        when(authRepository.findRoleByUserId(user.getUserId())).thenReturn(Optional.of(role));
        when(refreshTokenService.issue(user)).thenReturn(
                new RefreshTokenDetails("refresh-token", java.time.ZonedDateTime.now().plusDays(30)));

        AuthResponse response = service().login(
                new LoginRequest("  STUDENT01@EXAMPLE.COM  ", "secret123"));

        assertThat(response.userId()).isEqualTo(user.getUserId());
        assertThat(response.email()).isEqualTo("student01@example.com");
        verify(authRepository).findUserByEmail("STUDENT01@EXAMPLE.COM");
        verify(rateLimitService).check("student01@example.com", "LOGIN", 10, 900);
    }

    @Test
    void loginShouldRejectWrongPassword() {
        User user = User.create("student01", passwordService.hash("secret123"), "student01@example.com", "Student One");
        user.setStatus(UserStatus.VERIFIED);
        when(authRepository.findUserByUsername("student01")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service().login(new LoginRequest("student01", "wrong-password")))
                .isInstanceOf(AppException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void loginShouldRejectBannedUser() {
        User user = User.create("student01", passwordService.hash("secret123"), "student01@example.com", "Student One");
        user.setStatus(UserStatus.BANNED);
        when(authRepository.findUserByUsername("student01")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service().login(new LoginRequest("student01", "secret123")))
                .isInstanceOf(AppException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void loginShouldRejectUnverifiedUser() {
        User user = User.create("student01", passwordService.hash("secret123"), "student01@example.com", "Student One");
        when(authRepository.findUserByUsername("student01")).thenReturn(Optional.of(user));
        when(authChallengeService.issueOrReuseActive(user.getUserId(), AuthChallengePurpose.EMAIL_VERIFICATION, user.getEmail()))
                .thenReturn(new AuthChallengeService.IssuedChallenge("challenge-id", "123456"));

        assertThatThrownBy(() -> service().login(new LoginRequest("student01", "secret123")))
                .isInstanceOf(AppException.class)
                .satisfies(e -> {
                    AppException appException = (AppException) e;
                    assertThat(appException.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
                    assertThat(appException.getData()).isEqualTo(new RegisterResponse(user.getEmail(), "challenge-id"));
                });

        verify(emailOutboxService).enqueueVerificationOtp(user.getEmail(), "123456");
    }

    private AuthServiceImpl service() {
        return new AuthServiceImpl(authRepository, authMapper, passwordService, tokenService, otpService,
                emailOutboxService, refreshTokenService, authChallengeService, rateLimitService);
    }
}

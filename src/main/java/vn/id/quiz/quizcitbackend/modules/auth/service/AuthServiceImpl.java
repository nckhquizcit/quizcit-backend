package vn.id.quiz.quizcitbackend.modules.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.AuthResponse;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.ForgotPasswordRequest;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.ForgotPasswordResponse;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.LoginRequest;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.RegisterRequest;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.RegisterResponse;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.ResendRequest;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.ResetPasswordRequest;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.VerifyRequest;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.VerifyResetOtpRequest;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.VerifyResetOtpResponse;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.RefreshTokenRequest;
import vn.id.quiz.quizcitbackend.modules.auth.repository.AuthRepository;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.Role;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRoleAssignment;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserStatus;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.model.AuthChallengePurpose;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

import java.util.Optional;
import java.util.UUID;
import java.util.Locale;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthUseCase {
    private final AuthRepository authRepository;
    private final AuthMapper authMapper;
    private final PasswordService passwordService;
    private final TokenService tokenService;
    private final OtpService otpService;
    private final EmailOutboxService emailOutboxService;
    private final RefreshTokenService refreshTokenService;
    private final AuthChallengeService authChallengeService;
    private final AuthRateLimitService rateLimitService;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        rateLimitService.check(email, "REGISTER", 5, 900);
        if (authRepository.existsByUsername(username)) {
            throw new AppException(ErrorMessages.USERNAME_EXISTS, HttpStatus.CONFLICT);
        }
        if (authRepository.existsByEmail(email)) {
            throw new AppException(ErrorMessages.EMAIL_EXISTS, HttpStatus.CONFLICT);
        }

        String hashedPassword = passwordService.hash(request.password());
        User user = User.create(username, hashedPassword, email, request.displayName());
        User savedUser = authRepository.save(user);

        Role defaultRole = getOrCreateRole(UserRole.USER);
        authRepository.save(UserRoleAssignment.assign(savedUser.getUserId(), defaultRole.getRoleId()));

        return sendVerificationOtp(savedUser.getEmail());
    }

    @Override
    @Transactional
    public AuthResponse verify(VerifyRequest request) {
        var challenge = authChallengeService.verify(request.verificationToken(),
                AuthChallengePurpose.EMAIL_VERIFICATION, request.otp());
        User user = authRepository.findUserById(challenge.getUserId())
                .orElseThrow(() -> new AppException(ErrorMessages.ACCOUNT_NOT_FOUND, HttpStatus.NOT_FOUND));

        if (user.getStatus() == UserStatus.BANNED) {
            throw new AppException(ErrorMessages.ACCOUNT_BANNED, HttpStatus.FORBIDDEN);
        }

        // Idempotent: bam verify lan 2 (da VERIFIED) van cap token, khong bao loi.
        if (user.getStatus() != UserStatus.VERIFIED) {
            user.setStatus(UserStatus.VERIFIED);
            authRepository.save(user);
        }
        authChallengeService.consumeVerified(challenge.getChallengeId(), AuthChallengePurpose.EMAIL_VERIFICATION);

        UserRole role = resolveUserRole(user.getUserId());
        return issueTokenPair(user, role);
    }

    @Override
    @Transactional
    public RegisterResponse resend(ResendRequest request) {
        rateLimitService.check(request.email(), "RESEND_OTP", 3, 600);
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        Optional<User> found = authRepository.findUserByEmail(email);
        if (found.isEmpty() || found.get().getStatus() != UserStatus.UNVERIFIED) {
            passwordService.hash(otpService.generateCode());
            return new RegisterResponse(email, UUID.randomUUID().toString());
        }
        return sendVerificationOtp(found.get().getEmail());
    }

    private RegisterResponse sendVerificationOtp(String email) {
        User user = authRepository.findUserByEmail(email)
                .orElseThrow(() -> new AppException(ErrorMessages.ACCOUNT_NOT_FOUND, HttpStatus.NOT_FOUND));
        var challenge = authChallengeService.issue(user.getUserId(),
                AuthChallengePurpose.EMAIL_VERIFICATION, email);
        emailOutboxService.enqueueVerificationOtp(email, challenge.code());
        return new RegisterResponse(email, challenge.challengeId());
    }

    @Override
    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        rateLimitService.check(email, "FORGOT_PASSWORD", 3, 600);
        Optional<User> found = authRepository.findUserByEmail(email);
        if (found.isEmpty() || found.get().getStatus() == UserStatus.BANNED) {
            passwordService.hash(otpService.generateCode()); // cân bằng tương đối thời gian phản hồi
            return new ForgotPasswordResponse(email, UUID.randomUUID().toString());
        }
        var challenge = authChallengeService.issue(found.get().getUserId(),
                AuthChallengePurpose.PASSWORD_RESET, email);
        emailOutboxService.enqueuePasswordResetOtp(email, challenge.code());
        return new ForgotPasswordResponse(email, challenge.challengeId());
    }

    @Override
    @Transactional
    public VerifyResetOtpResponse verifyResetOtp(VerifyResetOtpRequest request) {
        var challenge = authChallengeService.verify(request.resetToken(),
                AuthChallengePurpose.PASSWORD_RESET, request.otp());
        User user = authRepository.findUserById(challenge.getUserId())
                .orElseThrow(() -> new AppException(ErrorMessages.ACCOUNT_NOT_FOUND, HttpStatus.NOT_FOUND));

        if (user.getStatus() == UserStatus.BANNED) {
            throw new AppException(ErrorMessages.ACCOUNT_BANNED, HttpStatus.FORBIDDEN);
        }

        return new VerifyResetOtpResponse(user.getEmail(), challenge.getChallengeId());
    }

    @Override
    @Transactional
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        var challenge = authChallengeService.consumeVerified(request.confirmedToken(),
                AuthChallengePurpose.PASSWORD_RESET);
        User user = authRepository.findUserById(challenge.getUserId())
                .orElseThrow(() -> new AppException(ErrorMessages.ACCOUNT_NOT_FOUND, HttpStatus.NOT_FOUND));

        if (user.getStatus() == UserStatus.BANNED) {
            throw new AppException(ErrorMessages.ACCOUNT_BANNED, HttpStatus.FORBIDDEN);
        }

        user.setPassword(passwordService.hash(request.newPassword()));
        user.revokeSessions();
        if (user.getStatus() != UserStatus.VERIFIED) {
            user.setStatus(UserStatus.VERIFIED);
        }
        authRepository.save(user);

        UserRole role = resolveUserRole(user.getUserId());
        return issueTokenPair(user, role);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String identifier = normalizeLoginIdentifier(request.identifier());
        rateLimitService.check(identifier.toLowerCase(Locale.ROOT), "LOGIN", 10, 900);
        User user = findUserByIdentifier(identifier)
                .orElseThrow(this::invalidCredentials);

        ensureUserCanAuthenticate(user);
        ensurePasswordMatches(request.password(), user.getPassword());
        upgradeLegacyPasswordIfNeeded(user, request.password());

        UserRole role = resolveUserRole(user.getUserId());
        return issueTokenPair(user, role);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        User user = refreshTokenService.consumeAndRotate(request.refreshToken());
        return issueTokenPair(user, resolveUserRole(user.getUserId()));
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    private AuthResponse issueTokenPair(User user, UserRole role) {
        TokenDetails access = tokenService.generate(user, role);
        RefreshTokenDetails refresh = refreshTokenService.issue(user);
        return authMapper.toAuthResponse(user, role, access, refresh);
    }

    private Optional<User> findUserByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }

        String normalizedIdentifier = identifier.trim();
        return authRepository.findUserByUsername(normalizedIdentifier)
                .or(() -> authRepository.findUserByEmail(normalizedIdentifier));
    }

    private String normalizeLoginIdentifier(String identifier) {
        return identifier == null ? "" : identifier.trim();
    }

    private void ensureUserCanAuthenticate(User user) {
        if (user.getStatus() == UserStatus.UNVERIFIED) {
            throw new AppException(ErrorMessages.ACCOUNT_UNVERIFIED, HttpStatus.FORBIDDEN);
        }
        if (user.getStatus() == UserStatus.BANNED) {
            throw new AppException(ErrorMessages.ACCOUNT_BANNED, HttpStatus.FORBIDDEN);
        }
    }

    private void ensurePasswordMatches(String rawPassword, String storedPassword) {
        if (!passwordService.matches(rawPassword, storedPassword)) {
            throw invalidCredentials();
        }
    }

    private void upgradeLegacyPasswordIfNeeded(User user, String rawPassword) {
        if (!passwordService.isHashed(user.getPassword())) {
            user.setPassword(passwordService.hash(rawPassword));
            authRepository.save(user);
        }
    }

    private Role getOrCreateRole(UserRole roleName) {
        return authRepository.findRoleByName(roleName)
                .orElseGet(() -> authRepository.save(Role.create(roleName)));
    }

    private UserRole resolveUserRole(String userId) {
        return authRepository.findRoleByUserId(userId)
                .map(Role::getName)
                .orElse(null);
    }

    private AppException invalidCredentials() {
        return new AppException(ErrorMessages.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
    }
}

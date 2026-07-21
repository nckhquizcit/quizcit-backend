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
import vn.id.quiz.quizcitbackend.modules.auth.repository.AuthRepository;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.Role;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRoleAssignment;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserStatus;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthUseCase {
    private final AuthRepository authRepository;
    private final AuthMapper authMapper;
    private final PasswordService passwordService;
    private final TokenService tokenService;
    private final OtpService otpService;
    private final EmailService emailService;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (authRepository.existsByUsername(request.username())) {
            throw new AppException("Tên đăng nhập đã tồn tại", HttpStatus.CONFLICT);
        }
        if (authRepository.existsByEmail(request.email())) {
            throw new AppException("Email đã tồn tại", HttpStatus.CONFLICT);
        }

        String hashedPassword = passwordService.hash(request.password());
        User user = User.create(request.username(), hashedPassword, request.email(), request.displayName());
        User savedUser = authRepository.save(user);

        Role defaultRole = getOrCreateRole(UserRole.USER);
        authRepository.save(UserRoleAssignment.assign(savedUser.getUserId(), defaultRole.getRoleId()));

        return sendVerificationOtp(savedUser.getEmail());
    }

    @Override
    @Transactional
    public AuthResponse verify(VerifyRequest request) {
        VerificationClaims claims = tokenService.parseVerificationToken(request.verificationToken());

        if (!passwordService.matches(request.otp(), claims.otpHash())) {
            throw new AppException("Mã xác thực không đúng", HttpStatus.UNAUTHORIZED);
        }

        User user = authRepository.findUserByEmail(claims.email())
                .orElseThrow(() -> new AppException(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        if (user.getStatus() == UserStatus.BANNED) {
            throw new AppException("Tài khoản đã bị khóa", HttpStatus.FORBIDDEN);
        }

        // Idempotent: bam verify lan 2 (da VERIFIED) van cap token, khong bao loi.
        if (user.getStatus() != UserStatus.VERIFIED) {
            user.setStatus(UserStatus.VERIFIED);
            authRepository.save(user);
        }

        UserRole role = resolveUserRole(user.getUserId());
        TokenDetails tokenDetails = tokenService.generate(user, role);
        return authMapper.toAuthResponse(user, role, tokenDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public RegisterResponse resend(ResendRequest request) {
        User user = authRepository.findUserByEmail(request.email().trim())
                .orElseThrow(() -> new AppException(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        if (user.getStatus() == UserStatus.VERIFIED) {
            throw new AppException("Tài khoản đã được xác thực", HttpStatus.CONFLICT);
        }
        if (user.getStatus() == UserStatus.BANNED) {
            throw new AppException("Tài khoản đã bị khóa", HttpStatus.FORBIDDEN);
        }

        return sendVerificationOtp(user.getEmail());
    }

    private RegisterResponse sendVerificationOtp(String email) {
        String code = otpService.generateCode();
        String otpHash = passwordService.hash(code);
        String verificationToken = tokenService.generateVerificationToken(email, otpHash);
        emailService.sendOtp(email, code);
        return new RegisterResponse(email, verificationToken);
    }

    @Override
    @Transactional(readOnly = true)
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        User user = authRepository.findUserByEmail(request.email().trim())
                .orElseThrow(() -> new AppException(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        String code = otpService.generateCode();
        String otpHash = passwordService.hash(code);
        String resetToken = tokenService.generatePasswordResetToken(user.getEmail(), otpHash);
        emailService.sendPasswordResetOtp(user.getEmail(), code);
        return new ForgotPasswordResponse(user.getEmail(), resetToken);
    }

    @Override
    @Transactional(readOnly = true)
    public VerifyResetOtpResponse verifyResetOtp(VerifyResetOtpRequest request) {
        VerificationClaims claims = tokenService.parsePasswordResetToken(request.resetToken());

        if (!passwordService.matches(request.otp(), claims.otpHash())) {
            throw new AppException("Mã xác thực không đúng", HttpStatus.UNAUTHORIZED);
        }

        User user = authRepository.findUserByEmail(claims.email())
                .orElseThrow(() -> new AppException(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        if (user.getStatus() == UserStatus.BANNED) {
            throw new AppException("Tài khoản đã bị khóa", HttpStatus.FORBIDDEN);
        }

        String confirmedToken = tokenService.generatePasswordResetConfirmedToken(user.getEmail());
        return new VerifyResetOtpResponse(user.getEmail(), confirmedToken);
    }

    @Override
    @Transactional
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        VerificationClaims claims = tokenService.parsePasswordResetConfirmedToken(request.confirmedToken());

        User user = authRepository.findUserByEmail(claims.email())
                .orElseThrow(() -> new AppException(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        if (user.getStatus() == UserStatus.BANNED) {
            throw new AppException("Tài khoản đã bị khóa", HttpStatus.FORBIDDEN);
        }

        user.setPassword(passwordService.hash(request.newPassword()));
        if (user.getStatus() != UserStatus.VERIFIED) {
            user.setStatus(UserStatus.VERIFIED);
        }
        authRepository.save(user);

        UserRole role = resolveUserRole(user.getUserId());
        TokenDetails tokenDetails = tokenService.generate(user, role);
        return authMapper.toAuthResponse(user, role, tokenDetails);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = findUserByIdentifier(request.identifier())
                .orElseThrow(this::invalidCredentials);

        ensureUserCanAuthenticate(user);
        ensurePasswordMatches(request.password(), user.getPassword());
        upgradeLegacyPasswordIfNeeded(user, request.password());

        UserRole role = resolveUserRole(user.getUserId());
        TokenDetails tokenDetails = tokenService.generate(user, role);
        return authMapper.toAuthResponse(user, role, tokenDetails);
    }

    private Optional<User> findUserByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }

        String normalizedIdentifier = identifier.trim();
        return authRepository.findUserByUsername(normalizedIdentifier)
                .or(() -> authRepository.findUserByEmail(normalizedIdentifier));
    }

    private void ensureUserCanAuthenticate(User user) {
        if (user.getStatus() == UserStatus.UNVERIFIED) {
            throw new AppException("Tài khoản chưa xác thực email", HttpStatus.FORBIDDEN);
        }
        if (user.getStatus() == UserStatus.BANNED) {
            throw new AppException("Tài khoản đã bị khóa", HttpStatus.FORBIDDEN);
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
        return new AppException("Thông tin đăng nhập không chính xác", HttpStatus.UNAUTHORIZED);
    }
}

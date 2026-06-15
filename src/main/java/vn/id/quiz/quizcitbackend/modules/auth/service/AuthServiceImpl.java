package vn.id.quiz.quizcitbackend.modules.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.AuthResponse;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.LoginRequest;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.RegisterRequest;
import vn.id.quiz.quizcitbackend.modules.auth.repository.AuthRepository;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.Role;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRoleAssignment;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserStatus;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthUseCase {
    private final AuthRepository authRepository;
    private final AuthMapper authMapper;
    private final PasswordService passwordService;
    private final TokenService tokenService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (authRepository.existsByUsername(request.username())) {
            throw new AppException("Ten dang nhap da ton tai", HttpStatus.CONFLICT);
        }
        if (authRepository.existsByEmail(request.email())) {
            throw new AppException("Email da ton tai", HttpStatus.CONFLICT);
        }

        String hashedPassword = passwordService.hash(request.password());
        User user = User.create(request.username(), hashedPassword, request.email(), request.displayName());
        User savedUser = authRepository.save(user);

        Role defaultRole = getOrCreateRole(UserRole.USER);
        authRepository.save(UserRoleAssignment.assign(savedUser.getUserId(), defaultRole.getRoleId()));

        TokenDetails tokenDetails = tokenService.generate(savedUser, defaultRole.getName());
        return authMapper.toAuthResponse(savedUser, defaultRole.getName(), tokenDetails);
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
        if (user.getStatus() == UserStatus.BANNED) {
            throw new AppException("Tai khoan da bi khoa", HttpStatus.FORBIDDEN);
        }
    }

    private void ensurePasswordMatches(String rawPassword, String storedPassword) {
        if (!passwordService.matches(rawPassword, storedPassword)) {
            throw invalidCredentials();
        }
    }

    private void upgradeLegacyPasswordIfNeeded(User user, String rawPassword) {
        if (!passwordService.isHashed(user.getPassword())) {
            user.changePassword(passwordService.hash(rawPassword));
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
        return new AppException("Thong tin dang nhap khong chinh xac", HttpStatus.UNAUTHORIZED);
    }
}

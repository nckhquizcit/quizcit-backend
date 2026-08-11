package vn.id.quiz.quizcitbackend.modules.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.model.RefreshToken;
import vn.id.quiz.quizcitbackend.modules.auth.repository.AuthRepository;
import vn.id.quiz.quizcitbackend.modules.auth.repository.persistence.JpaRefreshTokenRepository;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserStatus;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final JpaRefreshTokenRepository repository;
    private final AuthRepository authRepository;
    private final SecureRandom random = new SecureRandom();

    @Value("${quizcit.auth.refresh-token.expiration-seconds:2592000}")
    private long expirationSeconds;

    @Transactional
    public RefreshTokenDetails issue(User user) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        ZonedDateTime expiresAt = ZonedDateTime.now().plusSeconds(expirationSeconds);
        repository.save(RefreshToken.create(
                user.getUserId(), hash(raw), user.getTokenVersion(), expiresAt));
        return new RefreshTokenDetails(raw, expiresAt);
    }

    @Transactional
    public User consumeAndRotate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) throw invalid();
        RefreshToken stored = repository.findByTokenHashForUpdate(hash(rawToken)).orElseThrow(this::invalid);
        User user = authRepository.findUserById(stored.getUserId()).orElseThrow(this::invalid);
        if (!stored.isUsableAt(ZonedDateTime.now())
                || user.getStatus() != UserStatus.VERIFIED
                || !stored.getTokenVersion().equals(user.getTokenVersion())) {
            throw invalid();
        }
        stored.revoke();
        repository.save(stored);
        return user;
    }

    @Transactional
    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return;
        repository.findByTokenHashForUpdate(hash(rawToken)).ifPresent(token -> {
            token.revoke();
            repository.save(token);
        });
    }

    private String hash(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể băm refresh token", ex);
        }
    }

    private AppException invalid() {
        return new AppException(ErrorMessages.REFRESH_TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
    }
}

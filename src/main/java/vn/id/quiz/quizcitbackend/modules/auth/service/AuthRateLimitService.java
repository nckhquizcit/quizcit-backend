package vn.id.quiz.quizcitbackend.modules.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.model.AuthRateLimit;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.model.AuthRateLimitId;
import vn.id.quiz.quizcitbackend.modules.auth.repository.persistence.JpaAuthRateLimitRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

import java.time.ZonedDateTime;
import java.util.Locale;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AuthRateLimitService {
    private final JpaAuthRateLimitRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = AppException.class)
    public void check(String rawKey, String action, int maximumRequests, long windowSeconds) {
        String normalized = rawKey == null ? "unknown" : rawKey.trim().toLowerCase(Locale.ROOT);
        String key = hash(normalized);
        ZonedDateTime now = ZonedDateTime.now();
        AuthRateLimitId id = new AuthRateLimitId(key, action);
        AuthRateLimit limit = repository.findById(id).orElse(null);
        if (limit == null) {
            repository.save(AuthRateLimit.start(key, action, now));
            return;
        }
        if (limit.getBlockedUntil() != null && limit.getBlockedUntil().isAfter(now)) throw exceeded();
        if (limit.getWindowStart().plusSeconds(windowSeconds).isBefore(now)) {
            limit.reset(now);
        } else {
            limit.increment();
            if (limit.getRequestCount() > maximumRequests) {
                limit.blockUntil(limit.getWindowStart().plusSeconds(windowSeconds));
                repository.save(limit);
                throw exceeded();
            }
        }
        repository.save(limit);
    }

    private AppException exceeded() {
        return new AppException(ErrorMessages.RATE_LIMIT_EXCEEDED, HttpStatus.TOO_MANY_REQUESTS);
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể tạo khóa rate limit", ex);
        }
    }
}

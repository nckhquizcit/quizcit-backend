package vn.id.quiz.quizcitbackend.modules.auth.presentation.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "auth_rate_limits")
@IdClass(AuthRateLimitId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthRateLimit {
    @Id @Column(name = "rate_limit_key", length = 255)
    private String rateLimitKey;
    @Id @Column(length = 50)
    private String action;
    @Column(name = "window_start", nullable = false)
    private ZonedDateTime windowStart;
    @Column(name = "request_count", nullable = false)
    private Integer requestCount;
    @Column(name = "blocked_until")
    private ZonedDateTime blockedUntil;

    public static AuthRateLimit start(String key, String action, ZonedDateTime now) {
        AuthRateLimit value = new AuthRateLimit();
        value.rateLimitKey = key; value.action = action; value.windowStart = now; value.requestCount = 1;
        return value;
    }
    public void reset(ZonedDateTime now) { windowStart = now; requestCount = 1; blockedUntil = null; }
    public void increment() { requestCount++; }
    public void blockUntil(ZonedDateTime until) { blockedUntil = until; }
}

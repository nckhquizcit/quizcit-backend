package vn.id.quiz.quizcitbackend.modules.auth.presentation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    @Id
    @Column(name = "refresh_token_id", length = 50)
    private String refreshTokenId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "token_version", nullable = false)
    private Long tokenVersion;

    @Column(name = "expires_at", nullable = false)
    private ZonedDateTime expiresAt;

    @Column(name = "revoked_at")
    private ZonedDateTime revokedAt;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    public static RefreshToken create(String userId, String tokenHash, long tokenVersion, ZonedDateTime expiresAt) {
        return RefreshToken.builder()
                .refreshTokenId(UUID.randomUUID().toString())
                .userId(userId)
                .tokenHash(tokenHash)
                .tokenVersion(tokenVersion)
                .expiresAt(expiresAt)
                .createdAt(ZonedDateTime.now())
                .build();
    }

    public void revoke() {
        if (revokedAt == null) revokedAt = ZonedDateTime.now();
    }

    public boolean isUsableAt(ZonedDateTime now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }
}

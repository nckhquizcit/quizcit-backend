package vn.id.quiz.quizcitbackend.modules.auth.presentation.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "auth_challenges")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthChallenge {
    @Id @Column(name = "challenge_id", length = 50)
    private String challengeId;
    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AuthChallengePurpose purpose;
    @Column(name = "target_email", length = 100)
    private String targetEmail;
    @Column(name = "otp_hash", nullable = false, length = 255)
    private String otpHash;
    @Column(name = "failed_attempts", nullable = false)
    private Integer failedAttempts;
    @Column(name = "max_attempts", nullable = false)
    private Integer maxAttempts;
    @Column(name = "expires_at", nullable = false)
    private ZonedDateTime expiresAt;
    @Column(name = "resend_available_at", nullable = false)
    private ZonedDateTime resendAvailableAt;
    @Column(name = "verified_at")
    private ZonedDateTime verifiedAt;
    @Column(name = "consumed_at")
    private ZonedDateTime consumedAt;
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    public static AuthChallenge create(String userId, AuthChallengePurpose purpose,
                                       String targetEmail, String otpHash,
                                       ZonedDateTime expiresAt, ZonedDateTime resendAvailableAt) {
        return builder().challengeId(UUID.randomUUID().toString()).userId(userId)
                .purpose(purpose).targetEmail(targetEmail).otpHash(otpHash)
                .failedAttempts(0).maxAttempts(3).expiresAt(expiresAt)
                .resendAvailableAt(resendAvailableAt).createdAt(ZonedDateTime.now()).build();
    }

    public boolean isOpen(ZonedDateTime now) {
        return consumedAt == null && verifiedAt == null && expiresAt.isAfter(now)
                && failedAttempts < maxAttempts;
    }
    public void recordFailure() { failedAttempts++; if (failedAttempts >= maxAttempts) consumedAt = ZonedDateTime.now(); }
    public void verify() { verifiedAt = ZonedDateTime.now(); }
    public void consume() { consumedAt = ZonedDateTime.now(); }
}

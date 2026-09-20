package vn.id.quiz.quizcitbackend.modules.auth.presentation.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_outbox")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailOutbox {
    @Id @Column(name = "outbox_id", length = 50)
    private String outboxId;
    @Column(nullable = false, length = 100)
    private String recipient;
    @Column(nullable = false, length = 50)
    private String template;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
    @Column(name = "available_at", nullable = false)
    private ZonedDateTime availableAt;
    @Column(name = "sent_at")
    private ZonedDateTime sentAt;
    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount;
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    public static EmailOutbox create(String recipient, String template, String payload) {
        ZonedDateTime now = ZonedDateTime.now();
        return builder().outboxId(UUID.randomUUID().toString()).recipient(recipient)
                .template(template).payload(payload).createdAt(now).availableAt(now)
                .attemptCount(0).build();
    }
    public void markSent() { sentAt = ZonedDateTime.now(); lastError = null; payload = ""; }
    public void markFailed(String error) {
        attemptCount++;
        lastError = error == null ? "Lỗi gửi email" : error.substring(0, Math.min(error.length(), 1000));
        if (attemptCount >= 5) {
            sentAt = ZonedDateTime.now(); // Đánh dấu hoàn tất (failed) để ngừng retry
        } else {
            availableAt = ZonedDateTime.now().plusSeconds(Math.min(300, 5L * (1L << Math.min(attemptCount, 6))));
        }
    }
}

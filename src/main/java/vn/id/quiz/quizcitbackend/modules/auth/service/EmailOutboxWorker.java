package vn.id.quiz.quizcitbackend.modules.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.id.quiz.quizcitbackend.modules.auth.repository.persistence.JpaEmailOutboxRepository;
import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
public class EmailOutboxWorker {
    private final JpaEmailOutboxRepository repository;
    private final EmailService emailService;

    @Scheduled(fixedDelayString = "${quizcit.auth.email-outbox.poll-delay-ms:2000}")
    @Transactional
    public void deliverPending() {
        for (var message : repository.findTop20BySentAtIsNullAndAvailableAtLessThanEqualOrderByCreatedAt(ZonedDateTime.now())) {
            try {
                if (EmailOutboxService.RESET_PASSWORD_OTP.equals(message.getTemplate())) {
                    emailService.sendPasswordResetOtp(message.getRecipient(), message.getPayload());
                } else {
                    emailService.sendOtp(message.getRecipient(), message.getPayload());
                }
                message.markSent();
            } catch (RuntimeException ex) {
                message.markFailed(ex.getMessage());
            }
            repository.save(message);
        }
    }
}

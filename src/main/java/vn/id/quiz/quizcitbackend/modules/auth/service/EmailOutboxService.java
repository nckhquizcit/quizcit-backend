package vn.id.quiz.quizcitbackend.modules.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.model.EmailOutbox;
import vn.id.quiz.quizcitbackend.modules.auth.repository.persistence.JpaEmailOutboxRepository;

@Service
@RequiredArgsConstructor
public class EmailOutboxService {
    public static final String VERIFY_OTP = "VERIFY_OTP";
    public static final String RESET_PASSWORD_OTP = "RESET_PASSWORD_OTP";
    private final JpaEmailOutboxRepository repository;

    @Transactional
    public void enqueueVerificationOtp(String email, String code) {
        repository.save(EmailOutbox.create(email, VERIFY_OTP, code));
    }
    @Transactional
    public void enqueuePasswordResetOtp(String email, String code) {
        repository.save(EmailOutbox.create(email, RESET_PASSWORD_OTP, code));
    }
}

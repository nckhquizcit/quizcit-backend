package vn.id.quiz.quizcitbackend.modules.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.model.AuthChallenge;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.model.AuthChallengePurpose;
import vn.id.quiz.quizcitbackend.modules.auth.repository.persistence.JpaAuthChallengeRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class AuthChallengeService {
    private final JpaAuthChallengeRepository repository;
    private final PasswordService passwordService;
    private final OtpService otpService;

    @Value("${quizcit.auth.otp.expiration-seconds:300}")
    private long expirationSeconds;
    @Value("${quizcit.auth.otp.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    public record IssuedChallenge(String challengeId, String code) {}

    @Transactional
    public IssuedChallenge issue(String userId, AuthChallengePurpose purpose, String targetEmail) {
        ZonedDateTime now = ZonedDateTime.now();
        repository.findFirstByUserIdAndPurposeOrderByCreatedAtDesc(userId, purpose)
                .filter(previous -> previous.getConsumedAt() == null)
                .ifPresent(previous -> {
                    if (previous.getResendAvailableAt().isAfter(now)) {
                        throw new AppException(ErrorMessages.OTP_RESEND_TOO_SOON, HttpStatus.TOO_MANY_REQUESTS);
                    }
                    previous.consume();
                    repository.save(previous);
                });
        String code = otpService.generateCode();
        AuthChallenge challenge = AuthChallenge.create(userId, purpose, targetEmail,
                passwordService.hash(code), now.plusSeconds(expirationSeconds),
                now.plusSeconds(resendCooldownSeconds));
        repository.save(challenge);
        return new IssuedChallenge(challenge.getChallengeId(), code);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IssuedChallenge issueOrReuseActive(String userId, AuthChallengePurpose purpose, String targetEmail) {
        ZonedDateTime now = ZonedDateTime.now();
        var previousOpt = repository.findFirstByUserIdAndPurposeOrderByCreatedAtDesc(userId, purpose)
                .filter(previous -> previous.getConsumedAt() == null && previous.isOpen(now));

        if (previousOpt.isPresent()) {
            var previous = previousOpt.get();
            if (previous.getResendAvailableAt().isAfter(now)) {
                return new IssuedChallenge(previous.getChallengeId(), null);
            }
            previous.consume();
            repository.save(previous);
        }

        String code = otpService.generateCode();
        AuthChallenge challenge = AuthChallenge.create(userId, purpose, targetEmail,
                passwordService.hash(code), now.plusSeconds(expirationSeconds),
                now.plusSeconds(resendCooldownSeconds));
        repository.save(challenge);
        return new IssuedChallenge(challenge.getChallengeId(), code);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = AppException.class)
    public AuthChallenge verify(String challengeId, AuthChallengePurpose purpose, String otp) {
        AuthChallenge challenge = required(challengeId, purpose);
        if (!challenge.isOpen(ZonedDateTime.now())) {
            if (challenge.getFailedAttempts() >= challenge.getMaxAttempts()) {
                throw new AppException(ErrorMessages.OTP_ATTEMPTS_EXCEEDED, HttpStatus.TOO_MANY_REQUESTS);
            }
            throw invalidChallenge();
        }
        if (!passwordService.matches(otp, challenge.getOtpHash())) {
            challenge.recordFailure();
            repository.save(challenge);
            if (challenge.getFailedAttempts() >= challenge.getMaxAttempts()) {
                throw new AppException(ErrorMessages.OTP_ATTEMPTS_EXCEEDED, HttpStatus.TOO_MANY_REQUESTS);
            }
            throw new AppException(ErrorMessages.INVALID_OTP, HttpStatus.UNAUTHORIZED);
        }
        challenge.verify();
        repository.save(challenge);
        return challenge;
    }

    @Transactional
    public AuthChallenge consumeVerified(String challengeId, AuthChallengePurpose purpose) {
        AuthChallenge challenge = required(challengeId, purpose);
        if (challenge.getVerifiedAt() == null || challenge.getConsumedAt() != null
                || !challenge.getExpiresAt().isAfter(ZonedDateTime.now())) throw invalidChallenge();
        challenge.consume();
        repository.save(challenge);
        return challenge;
    }

    private AuthChallenge required(String id, AuthChallengePurpose purpose) {
        return repository.findByIdForUpdate(id).filter(c -> c.getPurpose() == purpose)
                .orElseThrow(this::invalidChallenge);
    }
    private AppException invalidChallenge() {
        return new AppException(ErrorMessages.INVALID_OR_EXPIRED_OTP, HttpStatus.BAD_REQUEST);
    }
}

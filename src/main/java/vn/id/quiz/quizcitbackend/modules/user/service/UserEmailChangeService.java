package vn.id.quiz.quizcitbackend.modules.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.model.AuthChallengePurpose;
import vn.id.quiz.quizcitbackend.modules.auth.repository.AuthRepository;
import vn.id.quiz.quizcitbackend.modules.auth.service.AuthChallengeService;
import vn.id.quiz.quizcitbackend.modules.auth.service.AuthRateLimitService;
import vn.id.quiz.quizcitbackend.modules.auth.service.EmailOutboxService;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.EmailChangeChallengeResponse;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UserResponse;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.VerifyEmailChangeOtpRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.VerifyOldEmailOtpRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserEmailChangeService {
    private final AuthRepository authRepository;
    private final AuthChallengeService challenges;
    private final AuthRateLimitService rateLimits;
    private final EmailOutboxService emailOutboxService;
    private final UserMapper mapper;

    @Transactional
    public EmailChangeChallengeResponse requestOldEmailOtp(String userId) {
        User user = requiredUser(userId);
        rateLimits.check(userId, "EMAIL_CHANGE", 3, 600);
        var challenge = challenges.issue(userId, AuthChallengePurpose.EMAIL_CHANGE_OLD, null);
        emailOutboxService.enqueueVerificationOtp(user.getEmail(), challenge.code());
        return new EmailChangeChallengeResponse(challenge.challengeId(), user.getEmail());
    }

    @Transactional
    public EmailChangeChallengeResponse verifyOldEmailOtp(String userId, VerifyOldEmailOtpRequest request) {
        String newEmail = request.newEmail().trim().toLowerCase(Locale.ROOT);
        User user = requiredUser(userId);
        if (user.getEmail().equalsIgnoreCase(newEmail) || authRepository.existsByEmail(newEmail)) {
            throw new AppException(ErrorMessages.EMAIL_EXISTS, HttpStatus.CONFLICT);
        }
        var old = challenges.verify(request.challengeId(), AuthChallengePurpose.EMAIL_CHANGE_OLD, request.otp());
        if (!old.getUserId().equals(userId)) throw new AppException(ErrorMessages.ACCESS_DENIED, HttpStatus.FORBIDDEN);
        challenges.consumeVerified(old.getChallengeId(), AuthChallengePurpose.EMAIL_CHANGE_OLD);
        var next = challenges.issue(userId, AuthChallengePurpose.EMAIL_CHANGE_NEW, newEmail);
        emailOutboxService.enqueueVerificationOtp(newEmail, next.code());
        return new EmailChangeChallengeResponse(next.challengeId(), newEmail);
    }

    @Transactional
    public UserResponse confirmNewEmail(String userId, VerifyEmailChangeOtpRequest request) {
        var challenge = challenges.verify(request.challengeId(), AuthChallengePurpose.EMAIL_CHANGE_NEW, request.otp());
        if (!challenge.getUserId().equals(userId)) throw new AppException(ErrorMessages.ACCESS_DENIED, HttpStatus.FORBIDDEN);
        if (authRepository.existsByEmail(challenge.getTargetEmail())) {
            throw new AppException(ErrorMessages.EMAIL_EXISTS, HttpStatus.CONFLICT);
        }
        User user = requiredUser(userId);
        user.setEmail(challenge.getTargetEmail());
        user.revokeSessions();
        authRepository.save(user);
        challenges.consumeVerified(challenge.getChallengeId(), AuthChallengePurpose.EMAIL_CHANGE_NEW);
        UserRole role = authRepository.findRoleByUserId(userId).map(r -> r.getName()).orElse(null);
        return mapper.toUserResponse(user, role);
    }

    private User requiredUser(String userId) {
        return authRepository.findUserById(userId)
                .orElseThrow(() -> new AppException(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
    }
}

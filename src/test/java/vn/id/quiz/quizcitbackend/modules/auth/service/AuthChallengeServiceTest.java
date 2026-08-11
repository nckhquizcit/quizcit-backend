package vn.id.quiz.quizcitbackend.modules.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.model.AuthChallenge;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.model.AuthChallengePurpose;
import vn.id.quiz.quizcitbackend.modules.auth.repository.persistence.JpaAuthChallengeRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthChallengeServiceTest {
    @Mock JpaAuthChallengeRepository repository;
    private final PasswordService passwords = new PasswordService();

    @Test
    void thirdWrongOtpShouldInvalidateChallenge() {
        AuthChallenge challenge = AuthChallenge.create("user-id", AuthChallengePurpose.EMAIL_VERIFICATION,
                "user@example.com", passwords.hash("123456"),
                ZonedDateTime.now().plusMinutes(5), ZonedDateTime.now());
        when(repository.findByIdForUpdate(challenge.getChallengeId())).thenReturn(Optional.of(challenge));
        AuthChallengeService service = new AuthChallengeService(repository, passwords, new OtpService());

        assertThatThrownBy(() -> service.verify(challenge.getChallengeId(), challenge.getPurpose(), "000000"))
                .isInstanceOf(AppException.class);
        assertThatThrownBy(() -> service.verify(challenge.getChallengeId(), challenge.getPurpose(), "000000"))
                .isInstanceOf(AppException.class);
        assertThatThrownBy(() -> service.verify(challenge.getChallengeId(), challenge.getPurpose(), "000000"))
                .isInstanceOf(AppException.class).hasMessageContaining("vô hiệu");

        assertThat(challenge.getFailedAttempts()).isEqualTo(3);
        assertThat(challenge.getConsumedAt()).isNotNull();
    }

    @Test
    void correctOtpShouldVerifyOpenChallenge() {
        AuthChallenge challenge = AuthChallenge.create("user-id", AuthChallengePurpose.PASSWORD_RESET,
                "user@example.com", passwords.hash("123456"),
                ZonedDateTime.now().plusMinutes(5), ZonedDateTime.now());
        when(repository.findByIdForUpdate(challenge.getChallengeId())).thenReturn(Optional.of(challenge));
        AuthChallengeService service = new AuthChallengeService(repository, passwords, new OtpService());

        service.verify(challenge.getChallengeId(), challenge.getPurpose(), "123456");
        assertThat(challenge.getVerifiedAt()).isNotNull();
        assertThat(challenge.getConsumedAt()).isNull();
    }
}

package vn.id.quiz.quizcitbackend.modules.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.model.RefreshToken;
import vn.id.quiz.quizcitbackend.modules.auth.repository.AuthRepository;
import vn.id.quiz.quizcitbackend.modules.auth.repository.persistence.JpaRefreshTokenRepository;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserStatus;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {
    @Mock JpaRefreshTokenRepository repository;
    @Mock AuthRepository authRepository;

    @Test
    void refreshTokenShouldBeSingleUse() {
        User user = User.create("student", "hash", "student@example.com", "Student");
        user.setStatus(UserStatus.VERIFIED);
        RefreshToken stored = RefreshToken.create(user.getUserId(), "hash", 0,
                ZonedDateTime.now().plusDays(1));
        when(repository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(stored));
        when(authRepository.findUserById(user.getUserId())).thenReturn(Optional.of(user));
        RefreshTokenService service = new RefreshTokenService(repository, authRepository);

        assertThat(service.consumeAndRotate("opaque-token")).isSameAs(user);
        assertThat(stored.getRevokedAt()).isNotNull();
        assertThatThrownBy(() -> service.consumeAndRotate("opaque-token"))
                .isInstanceOf(AppException.class);
    }
}

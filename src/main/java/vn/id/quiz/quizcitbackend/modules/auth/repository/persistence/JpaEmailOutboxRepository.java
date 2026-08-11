package vn.id.quiz.quizcitbackend.modules.auth.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.model.EmailOutbox;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface JpaEmailOutboxRepository extends JpaRepository<EmailOutbox, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<EmailOutbox> findTop20BySentAtIsNullAndAvailableAtLessThanEqualOrderByCreatedAt(ZonedDateTime now);
}

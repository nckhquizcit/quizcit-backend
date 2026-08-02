package vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Attempt;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaAttemptRepository extends JpaRepository<Attempt, String> {
    Optional<Attempt> findByAttemptId(
            String attemptId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Attempt a where a.attemptId = :attemptId")
    Optional<Attempt> findByAttemptIdForUpdate(@Param("attemptId") String attemptId);

    List<Attempt> findByUserId(
            String userId);
}

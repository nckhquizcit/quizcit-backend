package vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Attempt;

public interface JpaAttemptRepository extends JpaRepository<Attempt, String> {
    Optional<Attempt> findByAttemptId(
            String attemptId);

    List<Attempt> findByUserId(
            String userId);
}
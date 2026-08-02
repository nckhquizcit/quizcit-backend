package vn.id.quiz.quizcitbackend.modules.progress.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.AbilityHistory;

import java.util.List;

public interface JpaAbilityHistoryRepository
        extends JpaRepository<AbilityHistory, String> {

    List<AbilityHistory> findByUserIdOrderByCreatedAtDesc(String userId);

    List<AbilityHistory> findByUserIdAndSkillIdOrderByCreatedAtDesc(
            String userId,
            String skillId);

    boolean existsBySkillId(String skillId);

    AbilityHistory save(
            AbilityHistory history);

    List<AbilityHistory> findByAttemptIdOrderByCreatedAtAsc(
            String attemptId);
}

package vn.id.quiz.quizcitbackend.modules.progress.repository.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.UserSkillAbility;

public interface JpaUserSkillAbilityRepository
        extends JpaRepository<UserSkillAbility, String> {

    Optional<UserSkillAbility> findByUserIdAndSkillId(
            String userId,
            String skillId);

    List<UserSkillAbility> findByUserId(
            String userId);

    @Query("""
            SELECT COALESCE(AVG(u.theta), 0)
            FROM UserSkillAbility u
            WHERE u.userId = :userId
            """)
    Double getAverageAbility(String userId);
}
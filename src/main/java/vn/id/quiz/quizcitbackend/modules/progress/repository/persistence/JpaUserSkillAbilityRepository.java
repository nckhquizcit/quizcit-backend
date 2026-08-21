package vn.id.quiz.quizcitbackend.modules.progress.repository.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.UserSkillAbility;

public interface JpaUserSkillAbilityRepository
        extends JpaRepository<UserSkillAbility, String> {

    Optional<UserSkillAbility> findByUserIdAndSkillId(
            String userId,
            String skillId);

    @Query("""
            SELECT u FROM UserSkillAbility u, Skill s
            WHERE u.skillId = s.skillId AND u.userId = :userId
            """)
    List<UserSkillAbility> findByUserId(
            @Param("userId") String userId);

    boolean existsBySkillId(String skillId);

    @Query("""
            SELECT COALESCE(AVG(u.theta), 0)
            FROM UserSkillAbility u, Skill s
            WHERE u.skillId = s.skillId AND u.userId = :userId
            """)
    Double getAverageAbility(@Param("userId") String userId);

    void deleteBySkillId(String skillId);
    void deleteByUserId(String userId);
}


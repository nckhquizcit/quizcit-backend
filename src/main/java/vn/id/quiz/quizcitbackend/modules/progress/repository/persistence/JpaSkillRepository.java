package vn.id.quiz.quizcitbackend.modules.progress.repository.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.Skill;

public interface JpaSkillRepository extends JpaRepository<Skill, String> {

    @Query(value = "SELECT skill_id, name FROM skills WHERE skill_id IN (:skillIds)", nativeQuery = true)
    List<Object[]> findSkillNamesByIdsNative(@Param("skillIds") Collection<String> skillIds);

    @Query(value = "SELECT name FROM skills WHERE skill_id = :skillId", nativeQuery = true)
    Optional<String> findSkillNameByIdNative(@Param("skillId") String skillId);
}


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

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM Skill s WHERE LOWER(TRIM(s.name)) = LOWER(TRIM(:name))")
    boolean existsByNameIgnoreCaseTrimmed(@Param("name") String name);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM Skill s WHERE LOWER(TRIM(s.name)) = LOWER(TRIM(:name)) AND s.skillId != :skillId")
    boolean existsByNameIgnoreCaseTrimmedAndSkillIdNot(@Param("name") String name, @Param("skillId") String skillId);

    @Query("SELECT s FROM Skill s WHERE LOWER(TRIM(s.name)) = LOWER(TRIM(:name))")
    Optional<Skill> findByNameIgnoreCaseTrimmed(@Param("name") String name);
}


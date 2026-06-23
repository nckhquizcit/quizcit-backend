package vn.id.quiz.quizcitbackend.modules.progress.repository;

import java.util.List;
import java.util.Optional;

import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.Skill;

/**
 * Domain repository cho {@link Skill}. Phase 2 (P1).
 */
public interface SkillRepository {
    List<Skill> findAll();
    Optional<Skill> findById(String skillId);
    Skill save(Skill skill);
    void deleteById(String skillId);
}

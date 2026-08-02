package vn.id.quiz.quizcitbackend.modules.progress.repository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.Skill;

/**
 * Domain repository cho {@link Skill}. Phase 2 (P1).
 */
public interface SkillRepository {
    List<Skill> findAll();
    Optional<Skill> findById(String skillId);
    default List<Skill> findByIds(Collection<String> skillIds) {
        return skillIds == null ? List.of() : skillIds.stream().map(this::findById)
                .flatMap(Optional::stream).toList();
    }
    Skill save(Skill skill);
    void deleteById(String skillId);
}

package vn.id.quiz.quizcitbackend.modules.progress.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    default void hardDeleteById(String skillId) {
        deleteById(skillId);
    }

    default Map<String, String> findSkillNamesByIds(Collection<String> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return Map.of();
        }
        return findByIds(skillIds).stream()
                .collect(java.util.stream.Collectors.toMap(
                        Skill::getSkillId,
                        Skill::getName,
                        (first, second) -> first));
    }

    default Optional<String> findSkillNameById(String skillId) {
        if (skillId == null || skillId.isBlank()) {
            return Optional.empty();
        }
        return findById(skillId).map(Skill::getName);
    }
}


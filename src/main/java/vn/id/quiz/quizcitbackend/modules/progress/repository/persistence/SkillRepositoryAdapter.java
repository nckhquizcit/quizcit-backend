package vn.id.quiz.quizcitbackend.modules.progress.repository.persistence;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.Skill;
import vn.id.quiz.quizcitbackend.modules.progress.repository.SkillRepository;

@Component
@RequiredArgsConstructor
public class SkillRepositoryAdapter implements SkillRepository {

    private final JpaSkillRepository jpa;

    @Override
    public List<Skill> findAll() {
        return jpa.findAll();
    }

    @Override
    public Optional<Skill> findById(String skillId) {
        return jpa.findById(skillId);
    }

    @Override
    public List<Skill> findByIds(Collection<String> skillIds) {
        return skillIds == null || skillIds.isEmpty() ? List.of() : jpa.findAllById(skillIds);
    }

    @Override
    public Skill save(Skill skill) {
        return jpa.save(skill);
    }

    @Override
    public void deleteById(String skillId) {
        jpa.findById(skillId).ifPresent(skill -> {
            skill.softDelete();
            jpa.save(skill);
        });
    }

    @Override
    public void hardDeleteById(String skillId) {
        jpa.deleteById(skillId);
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        return jpa.existsByNameIgnoreCaseTrimmed(name.trim());
    }

    @Override
    public boolean existsByNameIgnoreCaseAndSkillIdNot(String name, String skillId) {
        if (name == null || name.isBlank()) {
            return false;
        }
        return jpa.existsByNameIgnoreCaseTrimmedAndSkillIdNot(name.trim(), skillId);
    }

    @Override
    public Optional<Skill> findByNameIgnoreCase(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return jpa.findByNameIgnoreCaseTrimmed(name.trim());
    }

    @Override
    public Map<String, String> findSkillNamesByIds(Collection<String> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return Map.of();
        }
        List<Object[]> rows = jpa.findSkillNamesByIdsNative(skillIds);
        Map<String, String> result = new HashMap<>();
        for (Object[] row : rows) {
            if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                result.put((String) row[0], (String) row[1]);
            }
        }
        return result;
    }

    @Override
    public Optional<String> findSkillNameById(String skillId) {
        if (skillId == null || skillId.isBlank()) {
            return Optional.empty();
        }
        return jpa.findSkillNameByIdNative(skillId);
    }
}

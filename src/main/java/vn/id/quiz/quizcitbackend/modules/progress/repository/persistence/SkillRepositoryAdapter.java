package vn.id.quiz.quizcitbackend.modules.progress.repository.persistence;

import java.util.List;
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
    public Skill save(Skill skill) {
        return jpa.save(skill);
    }

    @Override
    public void deleteById(String skillId) {
        jpa.deleteById(skillId);
    }
}

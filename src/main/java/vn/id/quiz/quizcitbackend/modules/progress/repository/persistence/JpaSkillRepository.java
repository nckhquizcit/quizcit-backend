package vn.id.quiz.quizcitbackend.modules.progress.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.Skill;

public interface JpaSkillRepository
        extends JpaRepository<Skill, String> {
}

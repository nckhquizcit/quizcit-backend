package vn.id.quiz.quizcitbackend.modules.progress.repository.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.QuestionSkill;

public interface JpaQuestionSkillRepository
        extends JpaRepository<QuestionSkill, String> {

    List<QuestionSkill> findByQuestionId(String questionId);

    List<QuestionSkill> findBySkillId(String skillId);

    void deleteByQuestionId(String questionId);
}

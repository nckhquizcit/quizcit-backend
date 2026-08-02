package vn.id.quiz.quizcitbackend.modules.progress.repository.persistence;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.QuestionSkill;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.QuestionSkillId;

public interface JpaQuestionSkillRepository
        extends JpaRepository<QuestionSkill, QuestionSkillId> {

    List<QuestionSkill> findByQuestionId(String questionId);

    // Batch: lấy skill mapping của nhiều câu hỏi trong 1 truy vấn (tránh N+1).
    List<QuestionSkill> findByQuestionIdIn(Collection<String> questionIds);


    List<QuestionSkill> findBySkillId(String skillId);
    boolean existsBySkillId(String skillId);

    void deleteByQuestionId(String questionId);
}

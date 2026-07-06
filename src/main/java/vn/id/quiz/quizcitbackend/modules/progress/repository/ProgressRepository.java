package vn.id.quiz.quizcitbackend.modules.progress.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.AbilityHistory;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.QuestionSkill;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.Skill;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.UserSkillAbility;

public interface ProgressRepository {

    Skill save(Skill skill);

    QuestionSkill save(QuestionSkill questionSkill);

    UserSkillAbility save(UserSkillAbility ability);

    List<QuestionSkill> findQuestionSkillsByQuestionId(
            String questionId);

    // Batch: lấy skill mapping của nhiều câu hỏi trong 1 truy vấn (tránh N+1).
    List<QuestionSkill> findQuestionSkillsByQuestionIds(
            Collection<String> questionIds);


    List<QuestionSkill> findQuestionSkillsBySkillId(
            String skillId);

    Optional<UserSkillAbility> findUserSkillAbility(
            String userId,
            String skillId);

    List<UserSkillAbility> findUserSkillAbilities(
            String userId);

    List<AbilityHistory> findAbilityHistoriesByUserId(String userId);

    List<AbilityHistory> findAbilityHistoriesByUserIdAndSkillId(
            String userId,
            String skillId);

    Double getAverageAbility(String userId);

    AbilityHistory save(
            AbilityHistory history);

    List<AbilityHistory> findAbilityHistoriesByAttemptId(
            String attemptId);
}
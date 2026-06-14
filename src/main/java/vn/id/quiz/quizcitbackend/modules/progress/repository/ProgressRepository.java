package vn.id.quiz.quizcitbackend.modules.progress.repository;

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
}
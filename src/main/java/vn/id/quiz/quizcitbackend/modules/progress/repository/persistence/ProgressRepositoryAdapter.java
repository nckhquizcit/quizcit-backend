package vn.id.quiz.quizcitbackend.modules.progress.repository.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.AbilityHistory;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.QuestionSkill;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.Skill;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.UserSkillAbility;
import vn.id.quiz.quizcitbackend.modules.progress.repository.ProgressRepository;

@Repository
@RequiredArgsConstructor
public class ProgressRepositoryAdapter
        implements ProgressRepository {

    private final JpaSkillRepository skillRepository;

    private final JpaQuestionSkillRepository questionSkillRepository;

    private final JpaUserSkillAbilityRepository userSkillAbilityRepository;
    private final JpaAbilityHistoryRepository abilityHistoryRepository;

    @Override
    public Skill save(Skill skill) {
        return skillRepository.save(skill);
    }

    @Override
    public QuestionSkill save(
            QuestionSkill questionSkill) {
        return questionSkillRepository.save(questionSkill);
    }

    @Override
    public UserSkillAbility save(
            UserSkillAbility ability) {
        return userSkillAbilityRepository.save(ability);
    }

    @Override
    public List<QuestionSkill> findQuestionSkillsByQuestionId(
            String questionId) {
        return questionSkillRepository.findByQuestionId(questionId);
    }

    @Override
    public List<QuestionSkill> findQuestionSkillsBySkillId(
            String skillId) {
        return questionSkillRepository.findBySkillId(skillId);
    }

    @Override
    public Optional<UserSkillAbility> findUserSkillAbility(
            String userId,
            String skillId) {
        return userSkillAbilityRepository
                .findByUserIdAndSkillId(userId, skillId);
    }

    @Override
    public List<UserSkillAbility> findUserSkillAbilities(
            String userId) {
        return userSkillAbilityRepository.findByUserId(userId);
    }

    @Override
    public List<AbilityHistory> findAbilityHistoriesByUserId(String userId) {
        return abilityHistoryRepository.findByUserId(userId);
    }

    @Override
    public List<AbilityHistory> findAbilityHistoriesByUserIdAndSkillId(
            String userId,
            String skillId) {
        return abilityHistoryRepository.findByUserIdAndSkillId(userId, skillId);
    }
}

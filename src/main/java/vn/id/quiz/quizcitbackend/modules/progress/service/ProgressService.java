package vn.id.quiz.quizcitbackend.modules.progress.service;

import java.util.List;

import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.AbilityHistoryResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.UserSkillAbilityResponse;

public interface ProgressService {

    UserSkillAbilityResponse getUserSkillAbility(
            String userId,
            String skillId);

    List<UserSkillAbilityResponse> getUserSkillAbilities(
            String userId);

    List<AbilityHistoryResponse> getAbilityHistories(
            String userId);

    List<AbilityHistoryResponse> getAbilityHistoriesBySkill(
            String userId,
            String skillId);
    
    double getAverageAbility(String userId);
}
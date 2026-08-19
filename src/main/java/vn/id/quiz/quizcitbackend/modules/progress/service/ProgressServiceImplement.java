package vn.id.quiz.quizcitbackend.modules.progress.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.AbilityHistoryResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.UserSkillAbilityResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.AbilityHistory;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.UserSkillAbility;
import vn.id.quiz.quizcitbackend.modules.progress.repository.ProgressRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.SkillRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

@Service
@RequiredArgsConstructor
public class ProgressServiceImplement implements ProgressService {

    private final ProgressRepository progressRepository;
    private final SkillRepository skillRepository;

    @Override
    @Transactional(readOnly = true)
    public UserSkillAbilityResponse getUserSkillAbility(
            String userId,
            String skillId) {

        UserSkillAbility ability = progressRepository
                .findUserSkillAbility(userId, skillId)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy dữ liệu năng lực của người dùng",
                        HttpStatus.NOT_FOUND));

        String skillName = getSkillName(skillId);
        return toAbilityResponse(ability, skillName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSkillAbilityResponse> getUserSkillAbilities(
            String userId) {

        List<UserSkillAbility> abilities = progressRepository.findUserSkillAbilities(userId);
        if (abilities.isEmpty()) {
            return List.of();
        }

        Set<String> skillIds = abilities.stream()
                .map(UserSkillAbility::getSkillId)
                .collect(Collectors.toSet());
        Map<String, String> skillNames = loadSkillNames(skillIds);

        return abilities.stream()
                .map(ability -> toAbilityResponse(ability, skillNames.get(ability.getSkillId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbilityHistoryResponse> getAbilityHistories(
            String userId) {

        List<AbilityHistory> histories = progressRepository.findAbilityHistoriesByUserId(userId);
        if (histories.isEmpty()) {
            return List.of();
        }

        Set<String> skillIds = histories.stream()
                .map(AbilityHistory::getSkillId)
                .collect(Collectors.toSet());
        Map<String, String> skillNames = loadSkillNames(skillIds);

        return histories.stream()
                .map(history -> toHistoryResponse(history, skillNames.get(history.getSkillId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbilityHistoryResponse> getAbilityHistoriesBySkill(
            String userId,
            String skillId) {

        List<AbilityHistory> histories = progressRepository
                .findAbilityHistoriesByUserIdAndSkillId(userId, skillId);
        if (histories.isEmpty()) {
            return List.of();
        }

        String skillName = getSkillName(skillId);
        return histories.stream()
                .map(history -> toHistoryResponse(history, skillName))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public double getAverageAbility(
            String userId) {

        return progressRepository
                .getAverageAbility(userId);
    }

    private Map<String, String> loadSkillNames(Collection<String> skillIds) {
        return skillRepository.findSkillNamesByIds(skillIds);
    }

    private String getSkillName(String skillId) {
        return skillRepository.findSkillNameById(skillId)
                .or(() -> skillRepository.findById(skillId).map(vn.id.quiz.quizcitbackend.modules.progress.presentation.model.Skill::getName))
                .orElse(null);
    }

    private UserSkillAbilityResponse toAbilityResponse(UserSkillAbility ability, String skillName) {
        return new UserSkillAbilityResponse(
                ability.getId(), ability.getUserId(), ability.getSkillId(),
                skillName, ability.getTheta(), ability.getUpdatedAt());
    }

    private AbilityHistoryResponse toHistoryResponse(AbilityHistory history, String skillName) {
        return new AbilityHistoryResponse(
                history.getHistoryId(), history.getUserId(), history.getSkillId(),
                skillName, history.getTheta(), history.getDelta(),
                history.getAttemptId(), history.getCreatedAt());
    }
}

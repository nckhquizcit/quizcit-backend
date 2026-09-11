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
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.UserProgressOverviewResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.UserProgressSkillResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.UserSkillDetailOverviewResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.Skill;
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

    @Override
    @Transactional(readOnly = true)
    public UserProgressOverviewResponse getUserProgressOverview(String userId) {
        List<UserSkillAbility> abilities = progressRepository.findUserSkillAbilities(userId);
        int trackedSkillCount = abilities.size();
        Double averageTheta = trackedSkillCount > 0 ? progressRepository.getAverageAbility(userId) : null;
        Double averageThetaScaled = averageTheta != null
                ? Math.max(0.0, Math.min(4.0, (averageTheta + 3.0) / 6.0 * 4.0))
                : null;

        Map<String, Skill> skillsById = skillRepository.findAll().stream()
                .collect(Collectors.toMap(Skill::getSkillId, s -> s, (s1, s2) -> s1));

        List<UserProgressSkillResponse> skills = abilities.stream().map(ability -> {
            Skill skill = skillsById.get(ability.getSkillId());
            String name = skill != null && skill.getName() != null ? skill.getName() : "Kỹ năng";
            String description = skill != null && skill.getDescription() != null ? skill.getDescription() : "";
            return UserProgressSkillResponse.builder()
                    .id(ability.getId())
                    .skillId(ability.getSkillId())
                    .name(name)
                    .description(description)
                    .theta(ability.getTheta())
                    .updatedAt(ability.getUpdatedAt())
                    .build();
        }).toList();

        List<AbilityHistory> allHistories = progressRepository.findAbilityHistoriesByUserId(userId);
        int totalHistoryCount = allHistories.size();
        List<AbilityHistoryResponse> recentHistories = allHistories.stream()
                .limit(12)
                .map(history -> {
                    Skill skill = skillsById.get(history.getSkillId());
                    String skillName = skill != null ? skill.getName() : null;
                    return toHistoryResponse(history, skillName);
                })
                .toList();

        return UserProgressOverviewResponse.builder()
                .averageTheta(averageTheta)
                .averageThetaScaled(averageThetaScaled)
                .trackedSkillCount(trackedSkillCount)
                .totalHistoryCount(totalHistoryCount)
                .skills(skills)
                .recentHistories(recentHistories)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProgressSkillResponse> getUserSkillsSummary(String userId) {
        List<UserSkillAbility> abilities = progressRepository.findUserSkillAbilities(userId);
        if (abilities.isEmpty()) {
            return List.of();
        }

        Map<String, Skill> skillsById = skillRepository.findAll().stream()
                .collect(Collectors.toMap(Skill::getSkillId, s -> s, (s1, s2) -> s1));

        return abilities.stream().map(ability -> {
            Skill skill = skillsById.get(ability.getSkillId());
            String name = skill != null && skill.getName() != null ? skill.getName() : "Kỹ năng";
            String description = skill != null && skill.getDescription() != null ? skill.getDescription() : "";
            return UserProgressSkillResponse.builder()
                    .id(ability.getId())
                    .skillId(ability.getSkillId())
                    .name(name)
                    .description(description)
                    .theta(ability.getTheta())
                    .updatedAt(ability.getUpdatedAt())
                    .build();
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserSkillDetailOverviewResponse getUserSkillDetailOverview(String userId, String skillId) {
        Skill targetSkill = skillRepository.findById(skillId)
                .orElseThrow(() -> new AppException("Không tìm thấy kỹ năng", HttpStatus.NOT_FOUND));

        java.util.Optional<UserSkillAbility> abilityOpt = progressRepository.findUserSkillAbility(userId, skillId);
        Double currentTheta = abilityOpt.map(UserSkillAbility::getTheta).orElse(null);
        java.time.ZonedDateTime updatedAt = abilityOpt.map(UserSkillAbility::getUpdatedAt).orElse(null);

        List<AbilityHistory> histories = progressRepository.findAbilityHistoriesByUserIdAndSkillId(userId, skillId);
        List<AbilityHistoryResponse> historyResponses = histories.stream()
                .map(h -> toHistoryResponse(h, targetSkill.getName()))
                .toList();

        List<UserSkillAbility> allAbilities = progressRepository.findUserSkillAbilities(userId);
        Map<String, Skill> skillsById = skillRepository.findAll().stream()
                .collect(Collectors.toMap(Skill::getSkillId, s -> s, (s1, s2) -> s1));

        List<UserProgressSkillResponse> otherSkills = allAbilities.stream()
                .filter(a -> !targetSkill.getSkillId().equalsIgnoreCase(a.getSkillId()))
                .map(ability -> {
                    Skill skill = skillsById.get(ability.getSkillId());
                    String name = skill != null && skill.getName() != null ? skill.getName() : "Kỹ năng";
                    String description = skill != null && skill.getDescription() != null ? skill.getDescription() : "";
                    return UserProgressSkillResponse.builder()
                            .id(ability.getId())
                            .skillId(ability.getSkillId())
                            .name(name)
                            .description(description)
                            .theta(ability.getTheta())
                            .updatedAt(ability.getUpdatedAt())
                            .build();
                })
                .toList();

        return UserSkillDetailOverviewResponse.builder()
                .skillId(targetSkill.getSkillId())
                .name(targetSkill.getName())
                .description(targetSkill.getDescription())
                .currentTheta(currentTheta)
                .updatedAt(updatedAt)
                .histories(historyResponses)
                .otherSkills(otherSkills)
                .build();
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

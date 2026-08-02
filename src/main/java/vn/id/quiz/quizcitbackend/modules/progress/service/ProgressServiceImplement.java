package vn.id.quiz.quizcitbackend.modules.progress.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.AbilityHistoryResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.UserSkillAbilityResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.UserSkillAbility;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.AbilityHistory;
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

        return toAbilityResponse(ability);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSkillAbilityResponse> getUserSkillAbilities(
            String userId) {

        return progressRepository.findUserSkillAbilities(userId).stream()
                .map(this::toAbilityResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbilityHistoryResponse> getAbilityHistories(
            String userId) {

        return progressRepository.findAbilityHistoriesByUserId(userId).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbilityHistoryResponse> getAbilityHistoriesBySkill(
            String userId,
            String skillId) {

        return progressRepository.findAbilityHistoriesByUserIdAndSkillId(userId, skillId).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public double getAverageAbility(
            String userId) {

        return progressRepository
                .getAverageAbility(userId);
    }

    private String getSkillName(String skillId) {
        return skillRepository.findById(skillId)
                .map(skill -> skill.getName())
                .orElse(null);
    }

    private UserSkillAbilityResponse toAbilityResponse(UserSkillAbility ability) {
        return new UserSkillAbilityResponse(
                ability.getId(), ability.getUserId(), ability.getSkillId(),
                getSkillName(ability.getSkillId()), ability.getTheta(), ability.getUpdatedAt());
    }

    private AbilityHistoryResponse toHistoryResponse(AbilityHistory history) {
        return new AbilityHistoryResponse(
                history.getHistoryId(), history.getUserId(), history.getSkillId(),
                getSkillName(history.getSkillId()), history.getTheta(), history.getDelta(),
                history.getAttemptId(), history.getCreatedAt());
    }
}

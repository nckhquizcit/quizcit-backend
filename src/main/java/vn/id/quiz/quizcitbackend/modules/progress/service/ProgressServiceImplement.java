package vn.id.quiz.quizcitbackend.modules.progress.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.AbilityHistoryResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.UserSkillAbilityResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.UserSkillAbility;
import vn.id.quiz.quizcitbackend.modules.progress.repository.ProgressRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

@Service
@RequiredArgsConstructor
public class ProgressServiceImplement implements ProgressService {

    private final ProgressRepository progressRepository;
    private final ProgressMapper mapper;

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

        return mapper.toUserSkillAbilityResponse(ability);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSkillAbilityResponse> getUserSkillAbilities(
            String userId) {

        return mapper.toUserSkillAbilityResponseList(
                progressRepository.findUserSkillAbilities(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbilityHistoryResponse> getAbilityHistories(
            String userId) {

        return mapper.toAbilityHistoryResponseList(
                progressRepository.findAbilityHistoriesByUserId(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbilityHistoryResponse> getAbilityHistoriesBySkill(
            String userId,
            String skillId) {

        return mapper.toAbilityHistoryResponseList(
                progressRepository
                        .findAbilityHistoriesByUserIdAndSkillId(
                                userId,
                                skillId));
    }
}
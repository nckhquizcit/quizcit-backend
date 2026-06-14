package vn.id.quiz.quizcitbackend.modules.progress.service;

import java.util.List;

import org.mapstruct.Mapper;

import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.AbilityHistoryResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.UserSkillAbilityResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.AbilityHistory;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.UserSkillAbility;

@Mapper(componentModel = "spring")
public interface ProgressMapper {

    UserSkillAbilityResponse toUserSkillAbilityResponse(
            UserSkillAbility ability);

    List<UserSkillAbilityResponse> toUserSkillAbilityResponseList(
            List<UserSkillAbility> abilities);

    AbilityHistoryResponse toAbilityHistoryResponse(
            AbilityHistory history);

    List<AbilityHistoryResponse> toAbilityHistoryResponseList(
            List<AbilityHistory> histories);
}
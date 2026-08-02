package vn.id.quiz.quizcitbackend.modules.progress.service;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.AbilityHistoryResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.UserSkillAbilityResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.AbilityHistory;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.UserSkillAbility;

@Mapper(componentModel = "spring")
public interface ProgressMapper {

    @Mapping(target = "skillName", ignore = true)
    UserSkillAbilityResponse toUserSkillAbilityResponse(
            UserSkillAbility ability);

    List<UserSkillAbilityResponse> toUserSkillAbilityResponseList(
            List<UserSkillAbility> abilities);

    @Mapping(target = "id", source = "historyId")
    @Mapping(target = "sourceAttemptId", source = "attemptId")
    @Mapping(target = "skillName", ignore = true)
    AbilityHistoryResponse toAbilityHistoryResponse(
            AbilityHistory history);

    List<AbilityHistoryResponse> toAbilityHistoryResponseList(
            List<AbilityHistory> histories);
}

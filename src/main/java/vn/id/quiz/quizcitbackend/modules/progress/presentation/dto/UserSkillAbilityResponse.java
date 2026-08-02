package vn.id.quiz.quizcitbackend.modules.progress.presentation.dto;

import java.time.ZonedDateTime;

public record UserSkillAbilityResponse(
        String id,
        String userId,
        String skillId,
        String skillName,
        Double theta,
        ZonedDateTime updatedAt) {
}

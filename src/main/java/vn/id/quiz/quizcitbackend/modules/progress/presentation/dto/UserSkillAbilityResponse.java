package vn.id.quiz.quizcitbackend.modules.progress.presentation.dto;

import java.time.ZonedDateTime;

public record UserSkillAbilityResponse(
        String id,
        String userId,
        String skillId,
        double theta,
        double standardError,
        ZonedDateTime updatedAt) {
}
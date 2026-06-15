package vn.id.quiz.quizcitbackend.modules.progress.presentation.dto;

import java.time.ZonedDateTime;

public record UserSkillAbilityResponse(
        String id,
        String userId,
        String skillId,
        Double theta,
        Double standardError,
        ZonedDateTime updatedAt) {
}

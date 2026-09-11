package vn.id.quiz.quizcitbackend.modules.progress.presentation.dto;

import lombok.Builder;
import java.time.ZonedDateTime;

@Builder
public record UserProgressSkillResponse(
        String id,
        String skillId,
        String name,
        String description,
        Double theta,
        ZonedDateTime updatedAt
) {
}

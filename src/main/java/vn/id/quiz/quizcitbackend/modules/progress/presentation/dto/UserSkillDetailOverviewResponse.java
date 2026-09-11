package vn.id.quiz.quizcitbackend.modules.progress.presentation.dto;

import lombok.Builder;
import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record UserSkillDetailOverviewResponse(
        String skillId,
        String name,
        String description,
        Double currentTheta,
        ZonedDateTime updatedAt,
        List<AbilityHistoryResponse> histories,
        List<UserProgressSkillResponse> otherSkills
) {
}

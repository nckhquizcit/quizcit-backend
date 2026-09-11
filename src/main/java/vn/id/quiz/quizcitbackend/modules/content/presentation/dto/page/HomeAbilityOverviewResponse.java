package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.page;

import lombok.Builder;

@Builder
public record HomeAbilityOverviewResponse(
        Double averageTheta,
        int skillCount,
        String abilityLevel
) {
}

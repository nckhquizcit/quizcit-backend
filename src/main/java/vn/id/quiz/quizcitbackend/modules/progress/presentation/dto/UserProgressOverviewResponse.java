package vn.id.quiz.quizcitbackend.modules.progress.presentation.dto;

import lombok.Builder;
import java.util.List;

@Builder
public record UserProgressOverviewResponse(
        Double averageTheta,
        Double averageThetaScaled,
        int trackedSkillCount,
        int totalHistoryCount,
        List<UserProgressSkillResponse> skills,
        List<AbilityHistoryResponse> recentHistories
) {
}

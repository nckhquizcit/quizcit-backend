package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.page;

import lombok.Builder;
import java.util.List;

@Builder
public record HomeSummaryResponse(
        HomeGreetingResponse greeting,
        HomeAbilityOverviewResponse abilityOverview,
        List<HomeEnrolledSubjectResponse> enrolledSubjects,
        HomeStatsResponse stats
) {
}

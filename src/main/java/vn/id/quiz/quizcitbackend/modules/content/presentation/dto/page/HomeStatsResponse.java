package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.page;

import lombok.Builder;

@Builder
public record HomeStatsResponse(
        int totalEnrolledSubjects,
        int totalUnlockedChapters
) {
}

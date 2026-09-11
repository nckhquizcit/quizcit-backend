package vn.id.quiz.quizcitbackend.modules.user.presentation.dto;

public record AdminProfileSummaryResponse(
        UserResponse admin,
        AdminDataStatsResponse stats
) {
}

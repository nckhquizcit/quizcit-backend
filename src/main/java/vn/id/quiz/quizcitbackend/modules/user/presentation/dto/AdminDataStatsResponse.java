package vn.id.quiz.quizcitbackend.modules.user.presentation.dto;

public record AdminDataStatsResponse(
        long totalUsers,
        long totalSubjects,
        long totalQuestions,
        long totalQuizzes,
        long totalAttempts) {
}

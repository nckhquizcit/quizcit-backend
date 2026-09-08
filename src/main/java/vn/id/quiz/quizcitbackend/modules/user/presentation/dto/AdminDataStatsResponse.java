package vn.id.quiz.quizcitbackend.modules.user.presentation.dto;

public record AdminDataStatsResponse(
        long totalUsers,
        long totalSubjects,
        long totalQuestions,
        long totalQuizzes,
        long totalAttempts,
        long totalSkills) {

    public AdminDataStatsResponse(
            long totalUsers,
            long totalSubjects,
            long totalQuestions,
            long totalQuizzes,
            long totalAttempts) {
        this(totalUsers, totalSubjects, totalQuestions, totalQuizzes, totalAttempts, 0L);
    }
}

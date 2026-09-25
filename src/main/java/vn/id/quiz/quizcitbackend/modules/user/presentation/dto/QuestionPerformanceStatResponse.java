package vn.id.quiz.quizcitbackend.modules.user.presentation.dto;

public record QuestionPerformanceStatResponse(
        String questionId,
        String content,
        String subjectName,
        double difficultyB,
        long correctCount,
        long incorrectCount,
        long totalEligible,
        double correctRate,
        double incorrectRate,
        Double newDifficulty
) {}

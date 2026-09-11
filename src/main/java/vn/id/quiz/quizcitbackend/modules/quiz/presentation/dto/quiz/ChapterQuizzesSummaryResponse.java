package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz;

import java.util.List;

public record ChapterQuizzesSummaryResponse(
        String chapterId,
        String name,
        String chapterName,
        int displayOrder,
        List<QuizResponse> quizzes
) {
    public ChapterQuizzesSummaryResponse(String chapterId, String name, int displayOrder, List<QuizResponse> quizzes) {
        this(chapterId, name, name, displayOrder, quizzes != null ? quizzes : List.of());
    }
}

package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz;

import java.util.List;

public record SubjectQuizzesOverviewResponse(
        String subjectId,
        String subjectName,
        List<ChapterQuizzesSummaryResponse> chapters
) {
    public SubjectQuizzesOverviewResponse {
        if (chapters == null) {
            chapters = List.of();
        }
    }
}

package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter;

import java.util.List;

public record ChapterSummaryResponse(
        String chapterId,
        String subjectId,
        String name,
        int displayOrder,
        long questionCount,
        long theoryCount,
        boolean unlocked,
        List<ChapterSkillResponse> skills
) {
    public ChapterSummaryResponse(
            String chapterId,
            String subjectId,
            String name,
            int displayOrder,
            long questionCount,
            long theoryCount,
            boolean unlocked
    ) {
        this(chapterId, subjectId, name, displayOrder, questionCount, theoryCount, unlocked, List.of());
    }
}

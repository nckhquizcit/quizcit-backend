package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject;

import lombok.Builder;
import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record SubjectCatalogSummaryResponse(
        String subjectId,
        String courseCode,
        String name,
        String description,
        String imageUrl,
        int displayOrder,
        int openChapterCount,
        int totalChapterCount,
        boolean enrolled,
        ZonedDateTime enrolledAt,
        List<ChapterPreviewResponse> chapterPreview
) {
}

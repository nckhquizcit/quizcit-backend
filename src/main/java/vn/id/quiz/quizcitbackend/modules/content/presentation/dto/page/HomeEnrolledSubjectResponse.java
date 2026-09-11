package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.page;

import lombok.Builder;
import java.time.ZonedDateTime;

@Builder
public record HomeEnrolledSubjectResponse(
        String subjectId,
        String courseCode,
        String name,
        String description,
        String imageUrl,
        int displayOrder,
        int chapterCount,
        int unlockedChapterCount,
        int totalChapterCount,
        ZonedDateTime enrolledAt
) {
}

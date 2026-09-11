package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject;

import lombok.Builder;

@Builder
public record ChapterPreviewResponse(
        String chapterId,
        String name,
        int displayOrder
) {
}

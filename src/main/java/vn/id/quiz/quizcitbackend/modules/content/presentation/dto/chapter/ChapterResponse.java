package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter;

import java.time.ZonedDateTime;

public record ChapterResponse(
        String chapterId,
        String subjectId,
        String name,
        ZonedDateTime createdAt,
        int displayOrder
) {
    public ChapterResponse{
        if(createdAt==null){
            createdAt=ZonedDateTime.now();
        }
    }
}

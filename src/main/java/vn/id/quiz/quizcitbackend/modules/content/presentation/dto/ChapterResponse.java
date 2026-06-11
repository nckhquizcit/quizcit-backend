package vn.id.quiz.quizcitbackend.modules.content.presentation.dto;

import java.time.ZonedDateTime;

public record ChapterResponse(
        String chapterId,
        String subjectId,
        String name,
        ZonedDateTime createdAt
) {
    public ChapterResponse{
        if(createdAt==null){
            createdAt=ZonedDateTime.now();
        }
    }
}

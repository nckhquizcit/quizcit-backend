package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject;

import java.time.ZonedDateTime;

public record SubjectResponse(
        String subjectId,
        String courseCode,
        String name,
        String description,
        boolean visible,
        ZonedDateTime createdAt,
        int displayOrder,
        String imageUrl
) {
    public SubjectResponse{
        if(createdAt==null){
            createdAt=ZonedDateTime.now();
        }
    }
}

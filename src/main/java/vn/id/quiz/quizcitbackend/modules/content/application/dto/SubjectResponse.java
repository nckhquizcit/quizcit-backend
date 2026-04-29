package vn.id.quiz.quizcitbackend.modules.content.application.dto;

import java.time.ZonedDateTime;

public record SubjectResponse(
        String subjectId,
        String courseCode,
        String name,
        String description,
        ZonedDateTime createdAt
) {
    public SubjectResponse{
        if(createdAt==null){
            createdAt=ZonedDateTime.now();
        }
    }
}

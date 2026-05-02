package vn.id.quiz.quizcitbackend.modules.content.application.dto;

import java.time.ZonedDateTime;

public record TheoryResponse (
    String theoryId,
    String chapterId,
    String content,
    ZonedDateTime createdAt
){
    public TheoryResponse{
        if(createdAt==null){
            createdAt=ZonedDateTime.now();
        }
    }
}

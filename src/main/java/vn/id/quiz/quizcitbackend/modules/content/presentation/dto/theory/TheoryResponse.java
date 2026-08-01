package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.theory;

import java.time.ZonedDateTime;

public record TheoryResponse (
    String theoryId,
    String chapterId,
    String content,
    String contentFormat,
    ZonedDateTime createdAt
){
    public TheoryResponse{
        if(createdAt==null){
            createdAt=ZonedDateTime.now();
        }
    }
}

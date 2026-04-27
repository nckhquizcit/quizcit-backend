package vn.id.quiz.quizcitbackend.modules.content.application.dto;

import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Chapter;

import java.time.ZonedDateTime;

public record SubjectResponse(
        String subjectId,
        String courseCode,
        String name,
        String description,
        ZonedDateTime createdAt
) {
}

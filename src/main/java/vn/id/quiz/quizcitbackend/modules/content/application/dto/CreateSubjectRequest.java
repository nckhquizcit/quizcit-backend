package vn.id.quiz.quizcitbackend.modules.content.application.dto;

import java.time.ZonedDateTime;

public record CreateSubjectRequest(
        String subjectId,
        String courseCode,
        String name,
        String description
) {
}

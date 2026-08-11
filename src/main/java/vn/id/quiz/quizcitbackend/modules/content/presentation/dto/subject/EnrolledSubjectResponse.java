package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject;

import java.time.ZonedDateTime;

public record EnrolledSubjectResponse(
        String subjectId,
        String courseCode,
        String name,
        String description,
        ZonedDateTime createdAt,
        int displayOrder,
        ZonedDateTime enrolledAt
) {
}


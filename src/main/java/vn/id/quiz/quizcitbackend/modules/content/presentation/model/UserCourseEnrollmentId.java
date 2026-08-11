package vn.id.quiz.quizcitbackend.modules.content.presentation.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserCourseEnrollmentId implements Serializable {
    private String userId;
    private String subjectId;
}


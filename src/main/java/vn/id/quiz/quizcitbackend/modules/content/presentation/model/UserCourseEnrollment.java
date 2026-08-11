package vn.id.quiz.quizcitbackend.modules.content.presentation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(name = "user_course_enrollments")
@IdClass(UserCourseEnrollmentId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserCourseEnrollment {
    @Id
    @Column(name = "user_id", length = 50, nullable = false)
    private String userId;

    @Id
    @Column(name = "subject_id", length = 50, nullable = false)
    private String subjectId;

    @Column(name = "enrolled_at", nullable = false, insertable = false, updatable = false)
    private ZonedDateTime enrolledAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", insertable = false, updatable = false)
    private Subject subject;
}


package vn.id.quiz.quizcitbackend.modules.content.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.UserCourseEnrollment;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.UserCourseEnrollmentId;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaUserCourseEnrollmentRepository;
import vn.id.quiz.quizcitbackend.modules.user.repository.UserRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CourseEnrollmentServiceTest {
    private final JpaUserCourseEnrollmentRepository enrollments = mock(JpaUserCourseEnrollmentRepository.class);
    private final ContentRepository content = mock(ContentRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final SubjectVisibilityGuard visibility = mock(SubjectVisibilityGuard.class);
    private final CourseEnrollmentService service = new CourseEnrollmentService(enrollments, content, users, visibility);
    private Subject subject;

    @BeforeEach
    void setUp() {
        subject = Subject.builder().subjectId("subject-1").courseCode("CT101")
                .name("Lập trình").visible(true).displayOrder(1).build();
        when(users.existsById("user-1")).thenReturn(true);
        when(content.findSubjectById("subject-1")).thenReturn(Optional.of(subject));
    }

    @Test
    void enrollCreatesEnrollmentAtomically() {
        ZonedDateTime enrolledAt = ZonedDateTime.now();
        when(enrollments.insertIfAbsent("user-1", "subject-1")).thenReturn(1);
        when(enrollments.findById(new UserCourseEnrollmentId("user-1", "subject-1")))
                .thenReturn(Optional.of(new UserCourseEnrollment(
                        "user-1", "subject-1", enrolledAt, subject)));

        var response = service.enroll("user-1", "subject-1");

        assertThat(response.subjectId()).isEqualTo("subject-1");
        assertThat(response.enrolledAt()).isEqualTo(enrolledAt);
        verify(visibility).requireVisibleSubject("subject-1");
    }

    @Test
    void duplicateEnrollmentReturnsConflict() {
        when(enrollments.insertIfAbsent("user-1", "subject-1")).thenReturn(0);

        assertThatThrownBy(() -> service.enroll("user-1", "subject-1"))
                .isInstanceOf(AppException.class)
                .extracting("status").isEqualTo(HttpStatus.CONFLICT.value());
    }

    @Test
    void cancelMissingEnrollmentReturnsNotFound() {
        when(enrollments.deleteEnrollment("user-1", "subject-1")).thenReturn(0);

        assertThatThrownBy(() -> service.cancel("user-1", "subject-1"))
                .isInstanceOf(AppException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void listReturnsVisibleEnrolledSubjectsInRepositoryOrder() {
        ZonedDateTime enrolledAt = ZonedDateTime.now();
        when(enrollments.findAllByUserIdWithSubject("user-1")).thenReturn(List.of(
                new UserCourseEnrollment("user-1", "subject-1", enrolledAt, subject)));

        var response = service.getEnrolledSubjects("user-1");

        assertThat(response).singleElement().satisfies(value -> {
            assertThat(value.courseCode()).isEqualTo("CT101");
            assertThat(value.enrolledAt()).isEqualTo(enrolledAt);
        });
    }
}


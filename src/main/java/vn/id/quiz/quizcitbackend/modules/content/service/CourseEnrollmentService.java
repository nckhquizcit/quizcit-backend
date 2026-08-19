package vn.id.quiz.quizcitbackend.modules.content.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.EnrolledSubjectResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.UserCourseEnrollment;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaUserCourseEnrollmentRepository;
import vn.id.quiz.quizcitbackend.modules.user.repository.UserRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseEnrollmentService {
    private final JpaUserCourseEnrollmentRepository enrollmentRepository;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final SubjectVisibilityGuard visibilityGuard;

    @Transactional
    public EnrolledSubjectResponse enroll(String userId, String subjectId) {
        requireExistingUser(userId);
        visibilityGuard.requireVisibleSubject(subjectId);
        Subject subject = contentRepository.findSubjectById(subjectId)
                .orElseThrow(() -> new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND));
        if (ContentConstants.QUESTION_BANK_COURSE_CODE.equalsIgnoreCase(subject.getCourseCode())
                || ContentConstants.QUESTION_BANK_NAME.equalsIgnoreCase(subject.getName())) {
            throw new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        if (enrollmentRepository.insertIfAbsent(userId, subjectId) == 0) {
            throw new AppException(ErrorMessages.COURSE_ALREADY_ENROLLED, HttpStatus.CONFLICT);
        }

        return enrollmentRepository.findById(new vn.id.quiz.quizcitbackend.modules.content.presentation.model.UserCourseEnrollmentId(
                        userId, subjectId))
                .map(enrollment -> toResponse(subject, enrollment.getEnrolledAt()))
                .orElseGet(() -> toResponse(subject, null));
    }

    @Transactional
    public void cancel(String userId, String subjectId) {
        requireExistingUser(userId);
        if (enrollmentRepository.deleteEnrollment(userId, subjectId) == 0) {
            throw new AppException(ErrorMessages.COURSE_ENROLLMENT_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    public List<EnrolledSubjectResponse> getEnrolledSubjects(String userId) {
        requireExistingUser(userId);
        return enrollmentRepository.findAllByUserIdWithSubject(userId).stream()
                .filter(enrollment -> visibilityGuard.isAdministrator() || enrollment.getSubject().isVisible())
                .map(this::toResponse)
                .toList();
    }

    private void requireExistingUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
    }

    private EnrolledSubjectResponse toResponse(UserCourseEnrollment enrollment) {
        return toResponse(enrollment.getSubject(), enrollment.getEnrolledAt());
    }

    private EnrolledSubjectResponse toResponse(Subject subject, java.time.ZonedDateTime enrolledAt) {
        return new EnrolledSubjectResponse(
                subject.getSubjectId(), subject.getCourseCode(), subject.getName(), subject.getDescription(),
                subject.getCreatedAt(), subject.getDisplayOrder(), enrolledAt);
    }
}


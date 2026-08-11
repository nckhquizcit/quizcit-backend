package vn.id.quiz.quizcitbackend.modules.content.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.EnrolledSubjectResponse;
import vn.id.quiz.quizcitbackend.modules.content.service.CourseEnrollmentService;
import vn.id.quiz.quizcitbackend.shared.security.CustomUserPrincipal;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/course-enrollments")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CourseEnrollmentController {
    private final CourseEnrollmentService enrollmentService;

    @PostMapping("/{subjectId}")
    public ResponseEntity<ApiResponse<EnrolledSubjectResponse>> enroll(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String subjectId) {
        EnrolledSubjectResponse response = enrollmentService.enroll(principal.getUserId(), subjectId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                HttpStatus.CREATED, "Đăng ký môn học thành công", response));
    }

    @DeleteMapping("/{subjectId}")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String subjectId) {
        enrollmentService.cancel(principal.getUserId(), subjectId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EnrolledSubjectResponse>>> getEnrolledSubjects(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(enrollmentService.getEnrolledSubjects(principal.getUserId())));
    }
}


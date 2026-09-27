package vn.id.quiz.quizcitbackend.modules.quiz.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.QuizResultResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.StartQuizResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.ActiveAttemptResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.AttemptStateResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.SaveAttemptDraftRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.SubmitQuizRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.service.AttemptService;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
import vn.id.quiz.quizcitbackend.shared.security.CustomUserPrincipal;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class AttemptController {

    private final AttemptService attemptService;
    private static final String ACCESS_DENIED_MESSAGE = "Bạn không có quyền thực hiện thao tác này.";

    @PostMapping("/quizzes/{quizId}/start")
    public ResponseEntity<ApiResponse<StartQuizResponse>> startQuiz(
            @PathVariable String quizId,
            @RequestParam(required = false) String userId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        StartQuizResponse response = attemptService.startQuiz(resolveUserId(principal, userId), quizId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<ApiResponse<AttemptStateResponse>> getAttemptState(
            @PathVariable String attemptId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        assertCanAccessAttempt(principal, attemptId);
        return ResponseEntity.ok(ApiResponse.success(attemptService.getAttemptState(attemptId)));
    }

    @PutMapping("/attempts/{attemptId}/draft")
    public ResponseEntity<ApiResponse<Void>> saveDraft(
            @PathVariable String attemptId,
            @Valid @RequestBody SaveAttemptDraftRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        assertCanAccessAttempt(principal, attemptId);
        String ownerId = principal.isAdministrator()
                ? attemptService.getAttemptOwnerId(attemptId)
                : principal.getUserId();
        attemptService.saveDraft(ownerId, attemptId, request.getAnswers());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/users/{userId}/active-attempts")
    public ResponseEntity<ApiResponse<List<ActiveAttemptResponse>>> getActiveAttempts(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                attemptService.getActiveAttempts(resolveUserId(principal, userId))));
    }

    @PostMapping("/attempts/submit")
    public ResponseEntity<ApiResponse<QuizResultResponse>> submitQuiz(
            @Valid @RequestBody SubmitQuizRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        assertCanAccessAttempt(principal, request.getAttemptId());
        QuizResultResponse response = attemptService.submitQuiz(request);

        return ResponseEntity.ok(
                ApiResponse.success(response));
    }

    @PostMapping("/quizzes/{quizId}/attempts/{attemptId}/submit")
    public ResponseEntity<ApiResponse<QuizResultResponse>> submitQuizForAttempt(
            @PathVariable String quizId,
            @PathVariable String attemptId,
            @Valid @RequestBody SubmitQuizRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        request.setQuizId(quizId);
        request.setAttemptId(attemptId);

        assertCanAccessAttempt(principal, attemptId);
        QuizResultResponse response = attemptService.submitQuiz(request);

        return ResponseEntity.ok(
                ApiResponse.success(response));
    }

    @GetMapping("/attempts/{attemptId}/result")
    public ResponseEntity<ApiResponse<QuizResultResponse>> getResult(
            @PathVariable String attemptId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        assertCanAccessAttempt(principal, attemptId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        attemptService.getResult(attemptId)));
    }

    @GetMapping("/users/{userId}/attempts")
    public ResponseEntity<ApiResponse<List<QuizResultResponse>>> getUserResults(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        attemptService.getUserResults(resolveUserId(principal, userId))));
    }

    private String resolveUserId(
            CustomUserPrincipal principal,
            String requestedUserId) {

        requirePrincipal(principal);

        if (principal.isAdministrator()) {
            return isBlank(requestedUserId) ? principal.getUserId() : requestedUserId;
        }

        if (!isBlank(requestedUserId) && !principal.getUserId().equals(requestedUserId)) {
            throwForbidden();
        }

        return principal.getUserId();
    }

    private void assertCanAccessAttempt(
            CustomUserPrincipal principal,
            String attemptId) {

        requirePrincipal(principal);

        if (principal.isAdministrator()) {
            return;
        }

        String ownerUserId = attemptService.getAttemptOwnerId(attemptId);
        if (!principal.getUserId().equals(ownerUserId)) {
            throwForbidden();
        }
    }

    private void requirePrincipal(
            CustomUserPrincipal principal) {

        if (principal == null || isBlank(principal.getUserId())) {
            throw new AppException(
                    "Bạn cần đăng nhập để thực hiện thao tác này.",
                    HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void throwForbidden() {
        throw new AppException(
                ACCESS_DENIED_MESSAGE,
                HttpStatus.FORBIDDEN);
    }
}

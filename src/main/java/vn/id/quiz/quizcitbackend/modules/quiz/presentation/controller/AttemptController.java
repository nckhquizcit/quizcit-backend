package vn.id.quiz.quizcitbackend.modules.quiz.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.QuizResultResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.StartQuizResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.SubmitQuizRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.service.AttemptService;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;
import vn.id.quiz.quizcitbackend.shared.security.CustomUserPrincipal;
import vn.id.quiz.quizcitbackend.shared.security.PrincipalAccess;
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

    @PostMapping("/quizzes/{quizId}/start")
    public ResponseEntity<ApiResponse<StartQuizResponse>> startQuiz(
            @PathVariable String quizId,
            @RequestParam(required = false) String userId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        StartQuizResponse response = attemptService.startQuiz(PrincipalAccess.resolveUserId(principal, userId), quizId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
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
                        attemptService.getUserResults(PrincipalAccess.resolveUserId(principal, userId))));
    }

    private void assertCanAccessAttempt(
            CustomUserPrincipal principal,
            String attemptId) {

        PrincipalAccess.requirePrincipal(principal);

        if (principal.isAdministrator()) {
            return;
        }

        String ownerUserId = attemptService.getAttemptOwnerId(attemptId);
        if (!principal.getUserId().equals(ownerUserId)) {
            throw new AppException(ErrorMessages.ACCESS_DENIED, HttpStatus.FORBIDDEN);
        }
    }
}

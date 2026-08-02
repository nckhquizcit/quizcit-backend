package vn.id.quiz.quizcitbackend.modules.quiz.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.adaptive.AdaptiveBatchRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.adaptive.AdaptiveQuizResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.adaptive.StartAdaptiveQuizRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.service.AdaptiveQuizService;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
import vn.id.quiz.quizcitbackend.shared.security.CustomUserPrincipal;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdaptiveQuizController {
    private final AdaptiveQuizService adaptiveQuizService;

    @PostMapping("/subjects/{subjectId}/adaptive-attempts")
    public ResponseEntity<ApiResponse<AdaptiveQuizResponse>> start(
            @PathVariable String subjectId,
            @Valid @RequestBody StartAdaptiveQuizRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                adaptiveQuizService.start(userId(principal), subjectId, request.getTotalQuestion())));
    }

    @GetMapping("/adaptive-attempts/{attemptId}")
    public ResponseEntity<ApiResponse<AdaptiveQuizResponse>> state(
            @PathVariable String attemptId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(adaptiveQuizService.getState(userId(principal), attemptId)));
    }

    @PostMapping("/adaptive-attempts/{attemptId}/answers")
    public ResponseEntity<ApiResponse<AdaptiveQuizResponse>> submitBatch(
            @PathVariable String attemptId,
            @Valid @RequestBody AdaptiveBatchRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                adaptiveQuizService.submitBatch(userId(principal), attemptId, request.getAnswers())));
    }

    private String userId(CustomUserPrincipal principal) {
        if (principal == null || principal.getUserId() == null || principal.getUserId().isBlank()) {
            throw new AppException("Bạn cần đăng nhập để làm bài thích ứng", HttpStatus.UNAUTHORIZED);
        }
        return principal.getUserId();
    }
}

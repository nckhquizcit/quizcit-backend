package vn.id.quiz.quizcitbackend.modules.quiz.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.QuizResultResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.StartQuizResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.SubmitQuizRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.service.AttemptService;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @RequestParam String userId) {

        StartQuizResponse response = attemptService.startQuiz(userId, quizId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PostMapping("/attempts/submit")
    public ResponseEntity<ApiResponse<QuizResultResponse>> submitQuiz(
            @Valid @RequestBody SubmitQuizRequest request) {

        QuizResultResponse response = attemptService.submitQuiz(request);

        return ResponseEntity.ok(
                ApiResponse.success(response));
    }

    @GetMapping("/attempts/{attemptId}/result")
    public ResponseEntity<ApiResponse<QuizResultResponse>> getResult(
            @PathVariable String attemptId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        attemptService.getResult(attemptId)));
    }

    @GetMapping("/users/{userId}/attempts")
    public ResponseEntity<ApiResponse<List<QuizResultResponse>>> getUserResults(
            @PathVariable String userId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        attemptService.getUserResults(userId)));
    }
}
package vn.id.quiz.quizcitbackend.modules.quiz.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.CreateQuizRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.QuizResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.UpdateQuizRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.service.QuizService;
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
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/chapters/{chapterId}/quizzes")
    public ResponseEntity<ApiResponse<List<QuizResponse>>> getQuizzesByChapter(
            @PathVariable String chapterId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        quizService.getQuizzesByChapter(chapterId)));
    }

    @PostMapping("/quizzes")
    public ResponseEntity<ApiResponse<QuizResponse>> createQuiz(
            @Valid @RequestBody CreateQuizRequest request) {

        QuizResponse response = quizService.createQuiz(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/quizzes/{quizId}")
    public ResponseEntity<ApiResponse<QuizResponse>> getQuizById(
            @PathVariable String quizId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        quizService.getQuizById(quizId)));
    }

    @PutMapping("/quizzes/{quizId}")
    public ResponseEntity<ApiResponse<Void>> updateQuiz(
            @PathVariable String quizId,
            @Valid @RequestBody UpdateQuizRequest request) {

        quizService.updateQuiz(quizId, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật đề thi thành công",
                        null));
    }

    @DeleteMapping("/quizzes/{quizId}")
    public ResponseEntity<ApiResponse<Void>> deleteQuiz(
            @PathVariable String quizId) {

        quizService.deleteQuiz(quizId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(
                        "Xóa đề thi thành công",
                        null));
    }
}
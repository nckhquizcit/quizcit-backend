package vn.id.quiz.quizcitbackend.modules.quiz.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.CreateQuizRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.QuestionResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.QuizResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.UpdateQuizRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.service.QuestionService;
import vn.id.quiz.quizcitbackend.modules.quiz.service.QuizService;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

import java.util.List;
import vn.id.quiz.quizcitbackend.shared.dto.ReorderRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class QuizController {

    private final QuizService quizService;
    private final QuestionService questionService;

    @GetMapping("/chapters/{chapterId}/quizzes")
    public ResponseEntity<ApiResponse<List<QuizResponse>>> getQuizzesByChapter(
            @PathVariable String chapterId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        quizService.getQuizzesByChapter(chapterId)));
    }

    @PostMapping("/quizzes")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<QuizResponse>> createQuiz(
            @Valid @RequestBody CreateQuizRequest request) {

        QuizResponse response = quizService.createQuiz(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PutMapping("/chapters/{chapterId}/quizzes/display-order")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> reorderQuizzes(
            @PathVariable String chapterId,
            @Valid @RequestBody ReorderRequest request) {
        quizService.reorderQuizzes(chapterId, request);
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật thứ tự đề thi", null));
    }

    @GetMapping("/quizzes/{quizId}")
    public ResponseEntity<ApiResponse<QuizResponse>> getQuizById(
            @PathVariable String quizId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        quizService.getQuizById(quizId)));
    }

    /**
     * Phase 2 (P0 - Blocker): API trả về câu hỏi + lựa chọn cho FE làm bài.
     */
    @GetMapping("/quizzes/{quizId}/questions")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuestionsByQuiz(
            @PathVariable String quizId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        questionService.getQuestionsByQuiz(quizId)));
    }

    @PutMapping("/quizzes/{quizId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
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
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> deleteQuiz(
            @PathVariable String quizId,
            @RequestParam(name = "hard", defaultValue = "false") boolean hard) {

        if (hard) {
            quizService.hardDeleteQuiz(quizId);
        } else {
            quizService.deleteQuiz(quizId);
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(
                        "Xóa đề thi thành công",
                        null));
    }
}

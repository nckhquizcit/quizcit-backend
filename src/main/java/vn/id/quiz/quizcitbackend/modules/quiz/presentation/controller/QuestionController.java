package vn.id.quiz.quizcitbackend.modules.quiz.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.CreateQuestionRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.QuestionResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.UpdateQuestionRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.service.QuestionService;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/chapters/{chapterId}/questions")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuestionsByChapter(
            @PathVariable String chapterId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        questionService.getQuestionsByChapter(chapterId)));
    }

    @PostMapping("/questions")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<QuestionResponse>> createQuestion(
            @Valid @RequestBody CreateQuestionRequest request) {

        QuestionResponse response = questionService.createQuestion(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/questions/{questionId}")
    public ResponseEntity<ApiResponse<QuestionResponse>> getQuestionById(
            @PathVariable String questionId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        questionService.getQuestionById(questionId)));
    }

    @PutMapping("/questions/{questionId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> updateQuestion(
            @PathVariable String questionId,
            @Valid @RequestBody UpdateQuestionRequest request) {

        questionService.updateQuestion(questionId, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật câu hỏi thành công",
                        null));
    }

    @DeleteMapping("/questions/{questionId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(
            @PathVariable String questionId) {

        questionService.deleteQuestion(questionId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(
                        "Xóa câu hỏi thành công",
                        null));
    }
}

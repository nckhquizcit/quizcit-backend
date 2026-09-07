package vn.id.quiz.quizcitbackend.modules.quiz.presentation.controller;

import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.AssignQuestionPlacementRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.BulkAssignQuestionPlacementRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.CreateQuestionRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.BulkCreateQuestionItemResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.BulkCreateQuestionsRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.BulkCreateQuestionsResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.QuestionResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.UpdateQuestionRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.service.QuestionService;
import vn.id.quiz.quizcitbackend.shared.security.CustomUserPrincipal;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class QuestionController {

    private final QuestionService questionService;
    private final Validator validator;

    @GetMapping("/questions")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getAllQuestions() {
        return ResponseEntity.ok(ApiResponse.success(questionService.getAllQuestions()));
    }

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

    @PostMapping("/questions/bulk")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<BulkCreateQuestionsResponse>> createQuestionsBulk(
            @Valid @RequestBody BulkCreateQuestionsRequest request) {
        List<BulkCreateQuestionItemResponse> results = request.items().stream()
                .map(item -> {
                    try {
                        if (item == null || item.clientId() == null || item.clientId().isBlank()) {
                            throw new IllegalArgumentException("clientId không được để trống");
                        }
                        if (item.question() == null) {
                            throw new IllegalArgumentException("Dữ liệu câu hỏi không được để trống");
                        }
                        var violations = validator.validate(item.question());
                        if (!violations.isEmpty()) {
                            throw new IllegalArgumentException(violations.iterator().next().getMessage());
                        }
                        QuestionResponse created = questionService.createQuestion(item.question());
                        return new BulkCreateQuestionItemResponse(
                                item.clientId(), true, created.getQuestionId(), null);
                    } catch (RuntimeException exception) {
                        String message = exception instanceof AppException || exception instanceof IllegalArgumentException
                                ? exception.getMessage()
                                : "Không thể lưu câu hỏi này";
                        return new BulkCreateQuestionItemResponse(
                                item == null ? null : item.clientId(), false, null, message);
                    }
                })
                .toList();
        int successCount = (int) results.stream().filter(BulkCreateQuestionItemResponse::success).count();
        BulkCreateQuestionsResponse response = new BulkCreateQuestionsResponse(
                results.size(), successCount, results.size() - successCount, results);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/questions/{questionId}")
    public ResponseEntity<ApiResponse<QuestionResponse>> getQuestionById(
            @PathVariable String questionId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        questionService.getQuestionById(
                                questionId,
                                principal != null && principal.isAdministrator())));
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
            @PathVariable String questionId,
            @RequestParam(name = "hard", defaultValue = "false") boolean hard) {

        if (hard) {
            throw new AppException("Hệ thống chỉ hỗ trợ xóa mềm, không cho phép xóa cứng dữ liệu.", HttpStatus.BAD_REQUEST);
        }
        questionService.deleteQuestion(questionId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(
                        "Xóa câu hỏi thành công",
                        null));
    }

    @PatchMapping("/questions/{questionId}/placement")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> assignQuestionPlacement(
            @PathVariable String questionId,
            @Valid @RequestBody AssignQuestionPlacementRequest request) {

        questionService.assignQuestionPlacement(questionId, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Gắn môn học và chương cho câu hỏi thành công",
                        null));
    }

    @PatchMapping("/questions/placement")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> bulkAssignQuestionPlacement(
            @Valid @RequestBody BulkAssignQuestionPlacementRequest request) {

        questionService.bulkAssignQuestionPlacement(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Gắn môn học và chương cho danh sách câu hỏi thành công",
                        null));
    }
}

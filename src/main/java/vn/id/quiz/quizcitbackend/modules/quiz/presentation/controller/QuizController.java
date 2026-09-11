package vn.id.quiz.quizcitbackend.modules.quiz.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.QuestionResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.CreateQuizRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.QuizDetailWithQuestionsResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.QuizResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.SubjectQuizzesOverviewResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.UpdateQuizRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.service.QuestionService;
import vn.id.quiz.quizcitbackend.modules.quiz.service.QuizService;
import vn.id.quiz.quizcitbackend.shared.dto.ReorderRequest;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

import java.util.List;

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

    /**
     * Lấy danh sách đề thi theo chương.
     *
     * @deprecated Dành cho tác vụ nguyên tử. Đối với trang quản trị hiển thị đề thi theo môn, hãy sử dụng {@code /admin/subjects/{subjectId}/quizzes-overview}.
     */
    @Deprecated(since = "v1.1", forRemoval = false)
    @GetMapping({"/chapters/{chapterId}/quizzes", "/v1/chapters/{chapterId}/quizzes"})
    public ResponseEntity<ApiResponse<List<QuizResponse>>> getQuizzesByChapter(
            @PathVariable String chapterId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        quizService.getQuizzesByChapter(chapterId)));
    }

    @GetMapping({"/admin/subjects/{subjectId}/quizzes-overview", "/v1/admin/subjects/{subjectId}/quizzes-overview"})
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<SubjectQuizzesOverviewResponse>> getSubjectQuizzesOverview(
            @PathVariable String subjectId) {
        return ResponseEntity.ok(ApiResponse.success(quizService.getSubjectQuizzesOverview(subjectId)));
    }

    @GetMapping({"/admin/quizzes/{quizId}/detail-with-questions", "/v1/admin/quizzes/{quizId}/detail-with-questions"})
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<QuizDetailWithQuestionsResponse>> getQuizDetailWithQuestions(
            @PathVariable String quizId) {
        return ResponseEntity.ok(ApiResponse.success(quizService.getQuizDetailWithQuestions(quizId)));
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

    /**
     * Lấy chi tiết đề thi theo mã định danh.
     *
     * @deprecated Dành cho tác vụ nguyên tử. Đối với trang quản trị hiển thị đề thi kèm câu hỏi, hãy sử dụng {@code /admin/quizzes/{quizId}/detail-with-questions}.
     */
    @Deprecated(since = "v1.1", forRemoval = false)
    @GetMapping({"/quizzes/{quizId}", "/v1/quizzes/{quizId}"})
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
            throw new AppException("Hệ thống chỉ hỗ trợ xóa mềm, không cho phép xóa cứng dữ liệu.", HttpStatus.BAD_REQUEST);
        }
        quizService.deleteQuiz(quizId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(
                        "Xóa đề thi thành công",
                        null));
    }
}

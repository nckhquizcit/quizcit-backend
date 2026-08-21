package vn.id.quiz.quizcitbackend.modules.content.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.ChapterResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.CreateChapterRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.UpdateChapterRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.CreateSubjectRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.SubjectResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.UpdateSubjectRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.theory.CreateTheoryRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.theory.TheoryResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.theory.UpdateTheoryRequest;
import vn.id.quiz.quizcitbackend.modules.content.service.ContentService;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.QuizSummaryResponse;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

import java.util.List;
import vn.id.quiz.quizcitbackend.shared.dto.ReorderRequest;

@RestController
@RequestMapping("/api") // Đổi thành /api để quản lý chung các resource phẳng
@RequiredArgsConstructor
@Validated
public class ContentController {
    private final ContentService subjectUseCase;

    // =========================================================================
    // SUBJECT ENDPOINTS (/api/subjects)
    // =========================================================================

    @GetMapping("/subjects")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getAllSubject() {
        List<SubjectResponse> responses = subjectUseCase.getAllSubject();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/chapters")
    public ResponseEntity<ApiResponse<List<ChapterResponse>>> getAllAccessibleChapters() {
        return ResponseEntity.ok(ApiResponse.success(subjectUseCase.getAllAccessibleChapters()));
    }

    @GetMapping("/subjects/{subjectId}")
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubjectById(@PathVariable String subjectId) {
        SubjectResponse response = subjectUseCase.getSubjectById(subjectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/subjects")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<SubjectResponse>> createSubject(
            @Valid @RequestBody CreateSubjectRequest request
    ) {
        SubjectResponse response = subjectUseCase.createSubject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/subjects/{subjectId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> updateSubject(
            @PathVariable String subjectId,
            @Valid @RequestBody UpdateSubjectRequest request
    ) {
        subjectUseCase.updateSubject(subjectId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật học phần thành công", null));
    }

    @PutMapping("/subjects/display-order")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> reorderSubjects(@Valid @RequestBody ReorderRequest request) {
        subjectUseCase.reorderSubjects(request);
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật thứ tự môn học", null));
    }

    @DeleteMapping("/subjects/{subjectId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(
            @PathVariable String subjectId,
            @RequestParam(name = "hard", defaultValue = "false") boolean hard) {
        if (hard) {
            subjectUseCase.hardDeleteSubject(subjectId);
        } else {
            subjectUseCase.deleteSubject(subjectId);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success("Đã xóa thành công học phần", null));
    }

    // =========================================================================
    // CHAPTER ENDPOINTS (/api/chapters)
    // =========================================================================

    // Nested Route: Lấy danh sách chương CỦA 1 môn học
    @GetMapping("/subjects/{subjectId}/chapters")
    public ResponseEntity<ApiResponse<List<ChapterResponse>>> getAllChapters(@PathVariable String subjectId) {
        List<ChapterResponse> response = subjectUseCase.getAllChapters(subjectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Nested Route: Thêm chương VÀO 1 môn học
    @PostMapping("/subjects/{subjectId}/chapters")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<ChapterResponse>> addChapter(
            @PathVariable String subjectId,
            @Valid @RequestBody CreateChapterRequest request
    ) {
        ChapterResponse response = subjectUseCase.addChapter(subjectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    // Flat Route: Lấy chi tiết 1 chương bằng ID
    @GetMapping("/chapters/{chapterId}")
    public ResponseEntity<ApiResponse<ChapterResponse>> getChapterById(@PathVariable String chapterId) {
        ChapterResponse response = subjectUseCase.getChapterById(chapterId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Flat Route: Cập nhật chương
    @PutMapping("/chapters/{chapterId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> updateChapter(
            @PathVariable String chapterId,
            @Valid @RequestBody UpdateChapterRequest request
    ) {
        subjectUseCase.updateChapter(chapterId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật chương thành công", null));
    }

    @PutMapping("/subjects/{subjectId}/chapters/display-order")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> reorderChapters(
            @PathVariable String subjectId,
            @Valid @RequestBody ReorderRequest request) {
        subjectUseCase.reorderChapters(subjectId, request);
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật thứ tự chương", null));
    }

    // Flat Route: Xóa chương
    @DeleteMapping("/chapters/{chapterId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> deleteChapter(
            @PathVariable String chapterId,
            @RequestParam(name = "hard", defaultValue = "false") boolean hard) {
        if (hard) {
            subjectUseCase.hardDeleteChapter(chapterId);
        } else {
            subjectUseCase.deleteChapter(chapterId);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success("Đã xóa chương thành công", null));
    }

    // =========================================================================
    // THEORY ENDPOINTS (/api/theories)
    // =========================================================================

    // Nested Route: Lấy danh sách lý thuyết CỦA 1 chương
    @GetMapping("/chapters/{chapterId}/theories")
    public ResponseEntity<ApiResponse<List<TheoryResponse>>> getAllTheories(@PathVariable String chapterId) {
        List<TheoryResponse> response = subjectUseCase.getAllTheories(chapterId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/subjects/{subjectId}/theories")
    public ResponseEntity<ApiResponse<List<TheoryResponse>>> getAllTheoriesBySubject(@PathVariable String subjectId) {
        return ResponseEntity.ok(ApiResponse.success(subjectUseCase.getAllTheoriesBySubject(subjectId)));
    }

    @PostMapping("/subjects/{subjectId}/theories/reindex")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Integer>> reindexTheoriesBySubject(@PathVariable String subjectId) {
        int indexedChunks = subjectUseCase.reindexTheoriesBySubject(subjectId);
        return ResponseEntity.ok(ApiResponse.success(
                "Re-indexed " + indexedChunks + " theory chunks", indexedChunks));
    }

    // Nested Route: Thêm lý thuyết VÀO 1 chương
    @PostMapping("/chapters/{chapterId}/theories")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<TheoryResponse>> addTheory(
            @PathVariable String chapterId,
            @Valid @RequestBody CreateTheoryRequest request
    ) {
        TheoryResponse response = subjectUseCase.addTheory(chapterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    // Flat Route: Lấy chi tiết lý thuyết
    @GetMapping("/theories/{theoryId}")
    public ResponseEntity<ApiResponse<TheoryResponse>> getTheoryById(@PathVariable String theoryId) {
        TheoryResponse response = subjectUseCase.getTheoryById(theoryId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Flat Route: Cập nhật lý thuyết
    @PutMapping("/theories/{theoryId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> updateTheory(
            @PathVariable String theoryId,
            @Valid @RequestBody UpdateTheoryRequest request
    ) {
        subjectUseCase.updateTheory(theoryId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật nội dung lý thuyết thành công", null));
    }

    // Flat Route: Xóa lý thuyết
    @DeleteMapping("/theories/{theoryId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> deleteTheory(
            @PathVariable String theoryId,
            @RequestParam(name = "hard", defaultValue = "false") boolean hard) {
        if (hard) {
            subjectUseCase.hardDeleteTheory(theoryId);
        } else {
            subjectUseCase.deleteTheory(theoryId);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success("Đã xóa nội dung lý thuyết thành công", null));
    }

    // =========================================================================
    // Phase 2 (P1): Danh sách đề thi theo môn học
    // =========================================================================
    @GetMapping("/subjects/{subjectId}/quizzes")
    public ResponseEntity<ApiResponse<List<QuizSummaryResponse>>> getQuizzesBySubject(
            @PathVariable String subjectId) {
        List<QuizSummaryResponse> responses = subjectUseCase.getQuizzesBySubject(subjectId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}

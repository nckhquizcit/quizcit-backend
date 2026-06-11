package vn.id.quiz.quizcitbackend.modules.content.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.id.quiz.quizcitbackend.modules.content.service.ContentService;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.*;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

import java.util.List;

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

    @GetMapping("/subjects/{subjectId}")
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubjectById(@PathVariable String subjectId) {
        SubjectResponse response = subjectUseCase.getSubjectById(subjectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/subjects")
    public ResponseEntity<ApiResponse<SubjectResponse>> createSubject(
            @Valid @RequestBody CreateSubjectRequest request
    ) {
        SubjectResponse response = subjectUseCase.createSubject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/subjects/{subjectId}")
    public ResponseEntity<ApiResponse<Void>> updateSubject(
            @PathVariable String subjectId,
            @Valid @RequestBody UpdateSubjectRequest request
    ) {
        subjectUseCase.updateSubject(subjectId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật học phần thành công", null));
    }

    @DeleteMapping("/subjects/{subjectId}")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(@PathVariable String subjectId) {
        subjectUseCase.deleteSubject(subjectId);
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
    public ResponseEntity<ApiResponse<Void>> updateChapter(
            @PathVariable String chapterId,
            @Valid @RequestBody UpdateChapterRequest request
    ) {
        subjectUseCase.updateChapter(chapterId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật chương thành công", null));
    }

    // Flat Route: Xóa chương
    @DeleteMapping("/chapters/{chapterId}")
    public ResponseEntity<ApiResponse<Void>> deleteChapter(@PathVariable String chapterId) {
        subjectUseCase.deleteChapter(chapterId);
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

    // Nested Route: Thêm lý thuyết VÀO 1 chương
    @PostMapping("/chapters/{chapterId}/theories")
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
    public ResponseEntity<ApiResponse<Void>> updateTheory(
            @PathVariable String theoryId,
            @Valid @RequestBody UpdateTheoryRequest request
    ) {
        subjectUseCase.updateTheory(theoryId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật nội dung lý thuyết thành công", null));
    }

    // Flat Route: Xóa lý thuyết
    @DeleteMapping("/theories/{theoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteTheory(@PathVariable String theoryId) {
        subjectUseCase.deleteTheory(theoryId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success("Đã xóa nội dung lý thuyết thành công", null));
    }
}
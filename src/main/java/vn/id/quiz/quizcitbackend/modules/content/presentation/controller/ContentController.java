package vn.id.quiz.quizcitbackend.modules.content.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.ChapterEditContextResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.ChapterResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.ChapterSummaryResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.FlatChapterResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.CreateChapterRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.UpdateChapterRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.CreateSubjectRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.SubjectResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.SubjectCatalogSummaryResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.SubjectStudyOverviewResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.theory.TheoryReaderViewResponse;
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

    /**
     * Lấy danh sách chương người dùng có thể truy cập.
     *
     * @deprecated Dành cho tác vụ nguyên tử. Đối với trang danh mục môn học, hãy sử dụng {@code /content/subjects/catalog-summary}. Đối với trang chủ, hãy sử dụng {@code /pages/home-summary}.
     */
    @Deprecated(since = "v1.1", forRemoval = false)
    @GetMapping({"/chapters", "/v1/chapters"})
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
            throw new AppException("Hệ thống chỉ hỗ trợ xóa mềm, không cho phép xóa cứng dữ liệu.", HttpStatus.BAD_REQUEST);
        }
        subjectUseCase.deleteSubject(subjectId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success("Đã xóa thành công học phần", null));
    }

    // =========================================================================
    // CHAPTER ENDPOINTS (/api/chapters)
    // =========================================================================

    /**
     * Lấy danh sách chương của một môn học.
     *
     * @deprecated Dành cho tác vụ nguyên tử. Đối với trang quản trị hiển thị số lượng, hãy sử dụng {@code /admin/subjects/{subjectId}/chapters-summary}.
     */
    @Deprecated(since = "v1.1", forRemoval = false)
    @GetMapping({"/subjects/{subjectId}/chapters", "/v1/subjects/{subjectId}/chapters"})
    public ResponseEntity<ApiResponse<List<ChapterResponse>>> getAllChapters(@PathVariable String subjectId) {
        List<ChapterResponse> response = subjectUseCase.getAllChapters(subjectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping({"/admin/subjects/{subjectId}/chapters-summary", "/v1/admin/subjects/{subjectId}/chapters-summary"})
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<List<ChapterSummaryResponse>>> getChaptersSummary(
            @PathVariable String subjectId) {
        List<ChapterSummaryResponse> responses = subjectUseCase.getChaptersSummary(subjectId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping({"/admin/chapters-flat", "/v1/admin/chapters-flat"})
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<List<FlatChapterResponse>>> getAllFlatChapters() {
        List<FlatChapterResponse> responses = subjectUseCase.getAllFlatChapters();
        return ResponseEntity.ok(ApiResponse.success(responses));
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

    /**
     * Lấy chi tiết 1 chương bằng ID.
     *
     * @deprecated Dành cho tác vụ nguyên tử. Đối với trang quản trị soạn thảo/chỉnh sửa chương, hãy sử dụng {@code /admin/chapters/{chapterId}/edit-context}.
     */
    @Deprecated(since = "v1.1", forRemoval = false)
    @GetMapping({"/chapters/{chapterId}", "/v1/chapters/{chapterId}"})
    public ResponseEntity<ApiResponse<ChapterResponse>> getChapterById(@PathVariable String chapterId) {
        ChapterResponse response = subjectUseCase.getChapterById(chapterId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping({"/admin/chapters/{chapterId}/edit-context", "/v1/admin/chapters/{chapterId}/edit-context"})
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<ChapterEditContextResponse>> getChapterEditContext(
            @PathVariable String chapterId) {
        ChapterEditContextResponse response = subjectUseCase.getChapterEditContext(chapterId);
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
            throw new AppException("Hệ thống chỉ hỗ trợ xóa mềm, không cho phép xóa cứng dữ liệu.", HttpStatus.BAD_REQUEST);
        }
        subjectUseCase.deleteChapter(chapterId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success("Đã xóa chương thành công", null));
    }

    // =========================================================================
    // THEORY ENDPOINTS (/api/theories)
    // =========================================================================

    /**
     * Lấy danh sách lý thuyết của một chương.
     *
     * @deprecated Dành cho tác vụ nguyên tử. Đối với trang đọc bài lý thuyết, hãy sử dụng {@code /content/theories/{theoryId}/reader-view}.
     */
    @Deprecated(since = "v1.1", forRemoval = false)
    @GetMapping("/chapters/{chapterId}/theories")
    public ResponseEntity<ApiResponse<List<TheoryResponse>>> getAllTheories(@PathVariable String chapterId) {
        List<TheoryResponse> response = subjectUseCase.getAllTheories(chapterId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Lấy danh sách lý thuyết theo môn học.
     *
     * @deprecated Dành cho tác vụ nguyên tử. Đối với trang học tập môn học, hãy sử dụng {@code /content/subjects/{subjectId}/study-overview}.
     */
    @Deprecated(since = "v1.1", forRemoval = false)
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

    /**
     * Lấy chi tiết một bài lý thuyết.
     *
     * @deprecated Dành cho tác vụ nguyên tử hoặc quản trị. Đối với trang đọc bài lý thuyết, hãy sử dụng {@code /content/theories/{theoryId}/reader-view}.
     */
    @Deprecated(since = "v1.1", forRemoval = false)
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
            throw new AppException("Hệ thống chỉ hỗ trợ xóa mềm, không cho phép xóa cứng dữ liệu.", HttpStatus.BAD_REQUEST);
        }
        subjectUseCase.deleteTheory(theoryId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success("Đã xóa nội dung lý thuyết thành công", null));
    }

    // =========================================================================
    // Phase 2 (P1): Danh sách đề thi theo môn học
    // =========================================================================
    /**
     * Danh sách đề thi theo môn học.
     *
     * @deprecated Dành cho tác vụ nguyên tử. Đối với trang học tập môn học, hãy sử dụng {@code /content/subjects/{subjectId}/study-overview}.
     */
    @Deprecated(since = "v1.1", forRemoval = false)
    @GetMapping("/subjects/{subjectId}/quizzes")
    public ResponseEntity<ApiResponse<List<QuizSummaryResponse>>> getQuizzesBySubject(
            @PathVariable String subjectId) {
        List<QuizSummaryResponse> responses = subjectUseCase.getQuizzesBySubject(subjectId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // =========================================================================
    // Phase 3: STUDY OVERVIEW & THEORY READER VIEW
    // =========================================================================
    @GetMapping({
            "/content/subjects/{subjectId}/study-overview",
            "/v1/content/subjects/{subjectId}/study-overview",
            "/subjects/{subjectId}/study-overview",
            "/v1/subjects/{subjectId}/study-overview"
    })
    public ResponseEntity<ApiResponse<SubjectStudyOverviewResponse>> getSubjectStudyOverview(
            @PathVariable String subjectId) {
        SubjectStudyOverviewResponse response = subjectUseCase.getSubjectStudyOverview(subjectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // =========================================================================
    // Phase 4: CATALOG MÔN HỌC TỔNG HỢP
    // =========================================================================
    @GetMapping({
            "/content/subjects/catalog-summary",
            "/v1/content/subjects/catalog-summary",
            "/subjects/catalog-summary",
            "/v1/subjects/catalog-summary"
    })
    public ResponseEntity<ApiResponse<List<SubjectCatalogSummaryResponse>>> getSubjectCatalogSummary() {
        List<SubjectCatalogSummaryResponse> responses = subjectUseCase.getSubjectCatalogSummary();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping({
            "/content/theories/{theoryId}/reader-view",
            "/v1/content/theories/{theoryId}/reader-view",
            "/theories/{theoryId}/reader-view",
            "/v1/theories/{theoryId}/reader-view"
    })
    public ResponseEntity<ApiResponse<TheoryReaderViewResponse>> getTheoryReaderView(
            @PathVariable String theoryId) {
        TheoryReaderViewResponse response = subjectUseCase.getTheoryReaderView(theoryId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

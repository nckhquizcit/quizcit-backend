package vn.id.quiz.quizcitbackend.modules.content.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.*;
import vn.id.quiz.quizcitbackend.modules.content.application.service.SubjectUseCase;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@Validated
public class SubjectController {
    private final SubjectUseCase subjectUseCase;

    // =========================================================================
    // SUBJECT ENDPOINTS
    // =========================================================================

    @GetMapping
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getAllSubject() {
        List<SubjectResponse> responses = subjectUseCase.getAllSubject();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{subjectId}")
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubjectById(@PathVariable String subjectId) {
        SubjectResponse response = subjectUseCase.getSubjectById(subjectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SubjectResponse>> createSubject(
            @Valid @RequestBody CreateSubjectRequest request
    ) {
        SubjectResponse response = subjectUseCase.createSubject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{subjectId}")
    public ResponseEntity<ApiResponse<Void>> updateSubject(
            @PathVariable String subjectId,
            @Valid @RequestBody UpdateSubjectRequest request
    ) {
        subjectUseCase.updateSubject(subjectId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật học phần thành công", null));
    }

    @DeleteMapping("/{subjectId}")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(@PathVariable String subjectId) {
        subjectUseCase.deleteSubject(subjectId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success("Đã xóa thành công học phần.", null));
    }

    // =========================================================================
    // CHAPTER ENDPOINTS
    // =========================================================================

    @GetMapping("/{subjectId}/chapters")
    public ResponseEntity<ApiResponse<List<ChapterResponse>>> getAllChapters(@PathVariable String subjectId) {
        List<ChapterResponse> response = subjectUseCase.getAllChapters(subjectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/chapters/{chapterId}")
    public ResponseEntity<ApiResponse<ChapterResponse>> getChapterById(@PathVariable String chapterId) {
        ChapterResponse response = subjectUseCase.getChapterById(chapterId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{subjectId}/chapters")
    public ResponseEntity<ApiResponse<ChapterResponse>> addChapter(
            @PathVariable String subjectId,
            @Valid @RequestBody CreateChapterRequest request
    ) {
        ChapterResponse response = subjectUseCase.addChapter(subjectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{subjectId}/chapters/{chapterId}")
    public ResponseEntity<ApiResponse<Void>> updateChapter(
            @PathVariable String subjectId,
            @PathVariable String chapterId,
            @Valid @RequestBody UpdateChapterRequest request
    ) {
        subjectUseCase.updateChapter(subjectId, chapterId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật chương thành công", null));
    }

    @DeleteMapping("/{subjectId}/chapters/{chapterId}")
    public ResponseEntity<ApiResponse<Void>> deleteChapter(
            @PathVariable String subjectId,
            @PathVariable String chapterId
    ) {
        subjectUseCase.deleteChapter(subjectId, chapterId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success("Đã xóa chương thành công", null));
    }

    // =========================================================================
    // THEORY ENDPOINTS
    // =========================================================================
    @Deprecated
    @GetMapping("/{subjectId}/theories")
    public ResponseEntity<ApiResponse<List<TheoryResponse>>> getAllTheoriesFromSubject(@PathVariable String subjectId) {
        List<TheoryResponse> response = subjectUseCase.getAllTheoriesFromSubject(subjectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    @GetMapping("/{subjectId}/chapters/{chapterId}/theories")
        public ResponseEntity<ApiResponse<List<TheoryResponse>>> getAllTheories(
                @PathVariable String subjectId,
                @PathVariable String chapterId){
        List<TheoryResponse> response =subjectUseCase.getAllTheories(subjectId, chapterId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }



    @GetMapping("/theories/{theoryId}")
    public ResponseEntity<ApiResponse<TheoryResponse>> getTheoryById(@PathVariable String theoryId) {
        TheoryResponse response = subjectUseCase.getTheoryById(theoryId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{subjectId}/chapters/{chapterId}/theories")
    public ResponseEntity<ApiResponse<TheoryResponse>> addTheory(
            @PathVariable String subjectId,
            @PathVariable String chapterId,
            @Valid @RequestBody CreateTheoryRequest request
    ) {
        TheoryResponse response = subjectUseCase.addTheory(subjectId, chapterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{subjectId}/chapters/{chapterId}/theories/{theoryId}")
    public ResponseEntity<ApiResponse<Void>> updateTheory(
            @PathVariable String subjectId,
            @PathVariable String chapterId,
            @PathVariable String theoryId,
            @Valid @RequestBody UpdateTheoryRequest request
    ) {
        subjectUseCase.updateTheory(subjectId, chapterId, theoryId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật nội dung lý thuyết thành công", null));
    }

    @DeleteMapping("/{subjectId}/chapters/{chapterId}/theories/{theoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteTheory(
            @PathVariable String subjectId,
            @PathVariable String chapterId,
            @PathVariable String theoryId
    ) {
        subjectUseCase.deleteTheory(subjectId, chapterId, theoryId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success("Đã xóa nội dung lý thuyết thành công", null));
    }
}
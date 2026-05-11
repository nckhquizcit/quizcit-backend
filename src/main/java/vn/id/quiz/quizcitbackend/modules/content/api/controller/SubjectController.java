package vn.id.quiz.quizcitbackend.modules.content.api.controller;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.TheoryResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.*;
import vn.id.quiz.quizcitbackend.modules.content.application.service.SubjectUseCase;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@Validated
public class SubjectController {
    private final SubjectUseCase subjectUseCase;

    /**
     * Tạo mới một học phần (Aggregate Root)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SubjectResponse>> createSubject(
            @Valid @RequestBody CreateSubjectRequest request
    ) {
        SubjectResponse response = subjectUseCase.createSubject(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * Thêm một chương vào học phần
     * URL: POST /api/subjects/{subjectId}/chapters
     */
    @PostMapping("/{subjectId}/chapters")
    public ResponseEntity<ApiResponse<ChapterResponse>> addChapter(
            @PathVariable String subjectId,
            @Valid @RequestBody CreateChapterRequest request
    ) {
        // subjectId được ưu tiên lấy từ URL để đảm bảo đúng Resource Context
        ChapterResponse response = subjectUseCase.addChapter(subjectId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * Thêm nội dung lý thuyết vào một chương của học phần
     * URL: POST /api/subjects/{subjectId}/chapters/{chapterId}/theories
     */
    @PostMapping("/{subjectId}/chapters/{chapterId}/theories")
    public ResponseEntity<ApiResponse<TheoryResponse>> addTheory(
            @PathVariable String subjectId,
            @PathVariable String chapterId,
            @Valid @RequestBody CreateTheoryRequest request
    ) {
        TheoryResponse response = subjectUseCase.addTheory(subjectId, chapterId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }
}
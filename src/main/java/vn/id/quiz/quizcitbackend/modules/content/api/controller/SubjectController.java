package vn.id.quiz.quizcitbackend.modules.content.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.CreateSubjectRequest;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.SubjectResponse;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.UpdateSubjectRequest;
import vn.id.quiz.quizcitbackend.modules.content.application.service.SubjectService;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {
    private final SubjectService subjectService;

    // GET /api/subjects
    @GetMapping
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getAllSubjects() {
        List<SubjectResponse> response = subjectService.getAllSubject();
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    // POST /api/subjects
    @PostMapping
    public ResponseEntity<ApiResponse<SubjectResponse>> createSubject(@RequestBody @Valid CreateSubjectRequest request) {
        SubjectResponse response = subjectService.createSubject(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo học phần thành công", response));
    }

    // GET /api/subjects/{subjectId}
    @GetMapping("/{subjectId}")
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubject(@PathVariable String subjectId) {
        SubjectResponse response = subjectService.getSubject(subjectId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    // DELETE /api/subjects/{subjectId}
    @DeleteMapping("/{subjectId}")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(@PathVariable String subjectId) {
        subjectService.deleteSubject(subjectId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Đã xóa thành công học phần.", null));
    }

    // PATCH /api/subjects/{subjectId}
    @PatchMapping("/{subjectId}")
    public ResponseEntity<ApiResponse<SubjectResponse>> updateSubject(
            @PathVariable String subjectId,
            @RequestBody @Valid UpdateSubjectRequest request) {
        SubjectResponse response = subjectService.updateSubject(subjectId, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Đã cập nhật thông tin mới cho học phần thành công.", response));
    }
}
package vn.id.quiz.quizcitbackend.modules.content.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.CreateTheoryRequest;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.TheoryResponse;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.UpdateTheoryRequest;
import vn.id.quiz.quizcitbackend.modules.content.application.service.TheoryService;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/theories")
@RequiredArgsConstructor
public class TheoryController {
    private final TheoryService theoryService;

    @GetMapping("/{theoryId}")
    public ResponseEntity<ApiResponse<TheoryResponse>> getTheory(@PathVariable String theoryId) {
        TheoryResponse response = theoryService.getTheory(theoryId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TheoryResponse>>> getAllTheories() {
        List<TheoryResponse> response = theoryService.getAllTheories();
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TheoryResponse>> createTheory(@RequestBody @Valid CreateTheoryRequest request) {
        TheoryResponse response = theoryService.createTheory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đã thêm nội dung lý thuyết thành công.", response));
    }

    @PatchMapping("/{theoryId}")
    public ResponseEntity<ApiResponse<TheoryResponse>> updateTheory(
            @PathVariable String theoryId,
            @RequestBody @Valid UpdateTheoryRequest request) {
        TheoryResponse response = theoryService.updateTheory(theoryId, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Đã cập nhật nội dung lý thuyết thành công.", response));
    }

    @DeleteMapping("/{theoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteTheory(@PathVariable String theoryId) {
        theoryService.deleteTheory(theoryId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Đã xóa nội dung lý thuyết thành công.", null));
    }
}
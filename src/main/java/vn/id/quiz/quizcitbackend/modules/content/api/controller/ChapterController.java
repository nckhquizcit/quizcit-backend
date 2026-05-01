package vn.id.quiz.quizcitbackend.modules.content.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.ChangeSubjectRequest;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.ChapterResponse;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.CreateChapterRequest;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.UpdateChapterRequest;
import vn.id.quiz.quizcitbackend.modules.content.application.service.ChapterService;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
public class ChapterController {
    private final ChapterService chapterService;

    @GetMapping("/{chapterId}")
    public ResponseEntity<ApiResponse<ChapterResponse>> getChapter(@PathVariable String chapterId){
        ChapterResponse response = chapterService.getChapter(chapterId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }
    @PatchMapping
    public ResponseEntity<ApiResponse<List<ChapterResponse>>> changeSubject(@RequestBody ChangeSubjectRequest request){
        List<ChapterResponse> response = chapterService.changeSubject(request.sourceSubject(), request.targetSubject());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }
    @GetMapping
    public ResponseEntity<ApiResponse<List<ChapterResponse>>> getAllChapters(){
        List<ChapterResponse> response = chapterService.getAllChapter();
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }
    @PostMapping
    public ResponseEntity<ApiResponse<ChapterResponse>> createChapter(@RequestBody @Valid CreateChapterRequest request){
        ChapterResponse response = chapterService.createChapter(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đã thêm chương thành công.",response));
    }
    @PatchMapping("/{chapterId}")
    public ResponseEntity<ApiResponse<ChapterResponse>> updateChapter(@PathVariable String chapterId, @RequestBody @Valid UpdateChapterRequest request ){
        ChapterResponse response = chapterService.updateChapter(chapterId, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Đã cập nhật chương thành công.",response));
    }
    @DeleteMapping("/{chapterId}")
    public ResponseEntity<ApiResponse<Void>> deleteChapter(@PathVariable String chapterId){
        chapterService.deleteChapter(chapterId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Đã xóa chương thành công.",null));
    }

}

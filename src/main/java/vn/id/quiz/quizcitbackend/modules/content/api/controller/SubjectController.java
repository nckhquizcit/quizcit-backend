package vn.id.quiz.quizcitbackend.modules.content.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.CreateSubjectRequest;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.SubjectResponse;
import vn.id.quiz.quizcitbackend.modules.content.application.service.SubjectService;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {
    private final SubjectService subjectService;

    @PostMapping
    public ResponseEntity<ApiResponse<SubjectResponse>> create(@RequestBody CreateSubjectRequest request){
        SubjectResponse response = subjectService.createSubject(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo môn học thành công", response));
    }
    @GetMapping
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubject(@RequestParam String subjectId){
        SubjectResponse response = subjectService.getSubject(subjectId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }
}

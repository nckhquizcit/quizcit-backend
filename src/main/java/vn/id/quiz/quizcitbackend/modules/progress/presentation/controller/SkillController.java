package vn.id.quiz.quizcitbackend.modules.progress.presentation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.SkillRequest;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.SkillResponse;
import vn.id.quiz.quizcitbackend.modules.progress.service.SkillService;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

/**
 * Phase 2 (P1) - CRUD API cho {@code Skill}.
 *
 * <p>Quy tắc phân quyền (P1):</p>
 * <ul>
 *   <li>{@code GET} - Ai cũng có thể xem danh sách skill.</li>
 *   <li>{@code POST / PUT / DELETE} - Chỉ {@code ADMINISTRATOR}.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SkillResponse>>> getAllSkills() {
        return ResponseEntity.ok(ApiResponse.success(skillService.getAllSkills()));
    }

    @GetMapping("/{skillId}")
    public ResponseEntity<ApiResponse<SkillResponse>> getSkillById(
            @PathVariable String skillId) {
        return ResponseEntity.ok(ApiResponse.success(skillService.getSkillById(skillId)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<SkillResponse>> createSkill(
            @Valid @RequestBody SkillRequest request) {
        SkillResponse created = skillService.createSkill(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{skillId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<SkillResponse>> updateSkill(
            @PathVariable String skillId,
            @Valid @RequestBody SkillRequest request) {
        SkillResponse updated = skillService.updateSkill(skillId, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{skillId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> deleteSkill(@PathVariable String skillId) {
        skillService.deleteSkill(skillId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success("Đã xóa kỹ năng thành công", null));
    }
}

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.BulkCreateSkillItemResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.BulkCreateSkillsRequest;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.BulkCreateSkillsResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.SkillRequest;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.SkillResponse;
import vn.id.quiz.quizcitbackend.modules.progress.service.SkillService;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
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
    private final Validator validator;

    /**
     * Lấy toàn bộ danh sách kỹ năng.
     *
     * @deprecated Dành cho tác vụ nguyên tử hoặc quản trị. Đối với trang kỹ năng người học, hãy sử dụng {@code /progress/skills/summary-list}.
     */
    @Deprecated(since = "v1.1", forRemoval = false)
    @GetMapping
    public ResponseEntity<ApiResponse<List<SkillResponse>>> getAllSkills() {
        return ResponseEntity.ok(ApiResponse.success(skillService.getAllSkills()));
    }

    /**
     * Lấy chi tiết một kỹ năng theo ID.
     *
     * @deprecated Dành cho tác vụ nguyên tử hoặc quản trị. Đối với trang chi tiết kỹ năng người học, hãy sử dụng {@code /progress/users/{userId}/skills/{skillId}/detail-overview}.
     */
    @Deprecated(since = "v1.1", forRemoval = false)
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

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<BulkCreateSkillsResponse>> createSkillsBulk(
            @Valid @RequestBody BulkCreateSkillsRequest request) {
        List<BulkCreateSkillItemResponse> results = request.items().stream()
                .map(item -> {
                    try {
                        if (item == null || item.clientId() == null || item.clientId().isBlank()) {
                            throw new IllegalArgumentException("clientId không được để trống");
                        }
                        if (item.skill() == null) {
                            throw new IllegalArgumentException("Dữ liệu kỹ năng không được để trống");
                        }
                        var violations = validator.validate(item.skill());
                        if (!violations.isEmpty()) {
                            throw new IllegalArgumentException(violations.iterator().next().getMessage());
                        }
                        SkillResponse created = skillService.createSkill(item.skill());
                        return new BulkCreateSkillItemResponse(
                                item.clientId(), true, created, null);
                    } catch (RuntimeException exception) {
                        String message = exception instanceof AppException || exception instanceof IllegalArgumentException
                                ? exception.getMessage()
                                : "Không thể tạo kỹ năng";
                        return new BulkCreateSkillItemResponse(
                                item != null ? item.clientId() : null, false, null, message);
                    }
                })
                .toList();

        int successCount = (int) results.stream().filter(BulkCreateSkillItemResponse::success).count();
        int failureCount = results.size() - successCount;

        BulkCreateSkillsResponse response = new BulkCreateSkillsResponse(
                results.size(), successCount, failureCount, results);

        return ResponseEntity.ok(ApiResponse.success("Đã xử lý import kỹ năng hàng loạt", response));
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
    public ResponseEntity<ApiResponse<Void>> deleteSkill(
            @PathVariable String skillId,
            @RequestParam(name = "hard", defaultValue = "false") boolean hard) {
        if (hard) {
            throw new AppException("Hệ thống chỉ hỗ trợ xóa mềm, không cho phép xóa cứng dữ liệu.", HttpStatus.BAD_REQUEST);
        }
        skillService.deleteSkill(skillId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success("Đã xóa kỹ năng thành công", null));
    }
}

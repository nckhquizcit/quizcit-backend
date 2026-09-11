package vn.id.quiz.quizcitbackend.modules.progress.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.AbilityHistoryResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.UserProgressOverviewResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.UserProgressSkillResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.UserSkillDetailOverviewResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.UserSkillAbilityResponse;
import vn.id.quiz.quizcitbackend.modules.progress.service.ProgressService;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
import vn.id.quiz.quizcitbackend.shared.security.CustomUserPrincipal;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class ProgressController {

    private final ProgressService progressService;
    private static final String ACCESS_DENIED_MESSAGE = "Bạn không có quyền thực hiện thao tác này.";

    // ============================================================
    // USER SKILL ABILITY
    // ============================================================

    /**
     * Lấy danh sách năng lực theo kỹ năng của người dùng.
     *
     * @deprecated Dành cho tác vụ nguyên tử. Đối với trang tổng quan tiến độ, hãy sử dụng {@code /progress/users/{userId}/overview}. Đối với danh sách kỹ năng, hãy sử dụng {@code /progress/users/{userId}/skills-summary}.
     */
    @Deprecated(since = "v1.1", forRemoval = false)
    @GetMapping({"/users/{userId}/skills", "/v1/users/{userId}/skills"})
    public ResponseEntity<ApiResponse<List<UserSkillAbilityResponse>>> getUserSkillAbilities(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        progressService.getUserSkillAbilities(resolveUserId(principal, userId))));
    }

    /**
     * Lấy năng lực 1 kỹ năng của người dùng.
     *
     * @deprecated Dành cho tác vụ nguyên tử. Đối với trang chi tiết kỹ năng, hãy sử dụng {@code /progress/users/{userId}/skills/{skillId}/detail-overview}.
     */
    @Deprecated(since = "v1.1", forRemoval = false)
    @GetMapping({"/users/{userId}/skills/{skillId}", "/v1/users/{userId}/skills/{skillId}"})
    public ResponseEntity<ApiResponse<UserSkillAbilityResponse>> getUserSkillAbility(
            @PathVariable String userId,
            @PathVariable String skillId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        progressService.getUserSkillAbility(
                                resolveUserId(principal, userId),
                                skillId)));
    }

    // ============================================================
    // ABILITY HISTORY
    // ============================================================

    /**
     * Lấy lịch sử biến động năng lực của người dùng.
     *
     * @deprecated Dành cho tác vụ nguyên tử. Đối với trang tổng quan tiến độ, hãy sử dụng {@code /progress/users/{userId}/overview}.
     */
    @Deprecated(since = "v1.1", forRemoval = false)
    @GetMapping({"/users/{userId}/abilities/history", "/v1/users/{userId}/abilities/history"})
    public ResponseEntity<ApiResponse<List<AbilityHistoryResponse>>> getAbilityHistories(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        progressService.getAbilityHistories(resolveUserId(principal, userId))));
    }

    /**
     * Lấy lịch sử biến động năng lực của người dùng theo kỹ năng.
     *
     * @deprecated Dành cho tác vụ nguyên tử. Đối với trang chi tiết kỹ năng, hãy sử dụng {@code /progress/users/{userId}/skills/{skillId}/detail-overview}.
     */
    @Deprecated(since = "v1.1", forRemoval = false)
    @GetMapping({"/users/{userId}/skills/{skillId}/history", "/v1/users/{userId}/skills/{skillId}/history"})
    public ResponseEntity<ApiResponse<List<AbilityHistoryResponse>>> getAbilityHistoriesBySkill(
            @PathVariable String userId,
            @PathVariable String skillId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        progressService.getAbilityHistoriesBySkill(
                                resolveUserId(principal, userId),
                                skillId)));
    }

    // ============================================================
    // SUMMARY
    // ============================================================

    /**
     * Lấy điểm năng lực trung bình của người dùng.
     *
     * @deprecated Dành cho tác vụ nguyên tử. Đối với trang tổng quan tiến độ, hãy sử dụng {@code /progress/users/{userId}/overview}.
     */
    @Deprecated(since = "v1.1", forRemoval = false)
    @GetMapping({"/users/{userId}/abilities/average", "/v1/users/{userId}/abilities/average"})
    public ResponseEntity<ApiResponse<Double>> getAverageAbility(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        progressService.getAverageAbility(resolveUserId(principal, userId))));
    }

    // ============================================================
    // Phase 4: OPTIMIZED PROGRESS ENDPOINTS
    // ============================================================

    @GetMapping({"/progress/users/{userId}/overview", "/v1/progress/users/{userId}/overview"})
    public ResponseEntity<ApiResponse<UserProgressOverviewResponse>> getUserProgressOverview(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        progressService.getUserProgressOverview(resolveUserId(principal, userId))));
    }

    @GetMapping({
            "/progress/users/{userId}/skills-summary",
            "/v1/progress/users/{userId}/skills-summary",
            "/progress/skills/summary-list",
            "/v1/progress/skills/summary-list"
    })
    public ResponseEntity<ApiResponse<List<UserProgressSkillResponse>>> getUserSkillsSummary(
            @PathVariable(required = false) String userId,
            @RequestParam(name = "userId", required = false) String paramUserId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        String targetUserId = (userId != null && !userId.isBlank()) ? userId : paramUserId;
        return ResponseEntity.ok(
                ApiResponse.success(
                        progressService.getUserSkillsSummary(resolveUserId(principal, targetUserId))));
    }

    @GetMapping({
            "/progress/users/{userId}/skills/{skillId}/detail-overview",
            "/v1/progress/users/{userId}/skills/{skillId}/detail-overview",
            "/progress/users/{userId}/skills/{skillId}/detail",
            "/v1/progress/users/{userId}/skills/{skillId}/detail"
    })
    public ResponseEntity<ApiResponse<UserSkillDetailOverviewResponse>> getUserSkillDetailOverview(
            @PathVariable String userId,
            @PathVariable String skillId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        progressService.getUserSkillDetailOverview(
                                resolveUserId(principal, userId),
                                skillId)));
    }

    private String resolveUserId(
            CustomUserPrincipal principal,
            String requestedUserId) {

        if (principal == null || principal.getUserId() == null || principal.getUserId().isBlank()) {
            throw new AppException(
                    "Bạn cần đăng nhập để thực hiện thao tác này.",
                    HttpStatus.UNAUTHORIZED);
        }

        if (principal.isAdministrator()) {
            return requestedUserId == null || requestedUserId.isBlank()
                    ? principal.getUserId()
                    : requestedUserId;
        }

        if (requestedUserId != null && !requestedUserId.isBlank()
                && !principal.getUserId().equals(requestedUserId)) {
            throw new AppException(
                    ACCESS_DENIED_MESSAGE,
                    HttpStatus.FORBIDDEN);
        }

        return principal.getUserId();
    }
}

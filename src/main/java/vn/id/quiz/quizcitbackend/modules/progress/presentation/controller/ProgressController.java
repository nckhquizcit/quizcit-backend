package vn.id.quiz.quizcitbackend.modules.progress.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.AbilityHistoryResponse;
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

    @GetMapping("/users/{userId}/skills")
    public ResponseEntity<ApiResponse<List<UserSkillAbilityResponse>>> getUserSkillAbilities(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        progressService.getUserSkillAbilities(resolveUserId(principal, userId))));
    }

    @GetMapping("/users/{userId}/skills/{skillId}")
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

    @GetMapping("/users/{userId}/abilities/history")
    public ResponseEntity<ApiResponse<List<AbilityHistoryResponse>>> getAbilityHistories(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        progressService.getAbilityHistories(resolveUserId(principal, userId))));
    }

    @GetMapping("/users/{userId}/skills/{skillId}/history")
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

    @GetMapping("/users/{userId}/abilities/average")
    public ResponseEntity<ApiResponse<Double>> getAverageAbility(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        progressService.getAverageAbility(resolveUserId(principal, userId))));
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

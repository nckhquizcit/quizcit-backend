package vn.id.quiz.quizcitbackend.modules.progress.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.AbilityHistoryResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.UserSkillAbilityResponse;
import vn.id.quiz.quizcitbackend.modules.progress.service.ProgressService;
import vn.id.quiz.quizcitbackend.shared.security.CustomUserPrincipal;
import vn.id.quiz.quizcitbackend.shared.security.PrincipalAccess;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class ProgressController {

    private final ProgressService progressService;

    // ============================================================
    // USER SKILL ABILITY
    // ============================================================

    @GetMapping("/users/{userId}/skills")
    public ResponseEntity<ApiResponse<List<UserSkillAbilityResponse>>> getUserSkillAbilities(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        progressService.getUserSkillAbilities(PrincipalAccess.resolveUserId(principal, userId))));
    }

    @GetMapping("/users/{userId}/skills/{skillId}")
    public ResponseEntity<ApiResponse<UserSkillAbilityResponse>> getUserSkillAbility(
            @PathVariable String userId,
            @PathVariable String skillId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        progressService.getUserSkillAbility(
                                PrincipalAccess.resolveUserId(principal, userId),
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
                        progressService.getAbilityHistories(PrincipalAccess.resolveUserId(principal, userId))));
    }

    @GetMapping("/users/{userId}/skills/{skillId}/history")
    public ResponseEntity<ApiResponse<List<AbilityHistoryResponse>>> getAbilityHistoriesBySkill(
            @PathVariable String userId,
            @PathVariable String skillId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        progressService.getAbilityHistoriesBySkill(
                                PrincipalAccess.resolveUserId(principal, userId),
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
                        progressService.getAverageAbility(PrincipalAccess.resolveUserId(principal, userId))));
    }

}

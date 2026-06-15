package vn.id.quiz.quizcitbackend.modules.progress.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.AbilityHistoryResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.UserSkillAbilityResponse;
import vn.id.quiz.quizcitbackend.modules.progress.service.ProgressService;
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
            @PathVariable String userId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        progressService.getUserSkillAbilities(userId)));
    }

    @GetMapping("/users/{userId}/skills/{skillId}")
    public ResponseEntity<ApiResponse<UserSkillAbilityResponse>> getUserSkillAbility(
            @PathVariable String userId,
            @PathVariable String skillId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        progressService.getUserSkillAbility(
                                userId,
                                skillId)));
    }

    // ============================================================
    // ABILITY HISTORY
    // ============================================================

    @GetMapping("/users/{userId}/abilities/history")
    public ResponseEntity<ApiResponse<List<AbilityHistoryResponse>>> getAbilityHistories(
            @PathVariable String userId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        progressService.getAbilityHistories(userId)));
    }

    @GetMapping("/users/{userId}/skills/{skillId}/history")
    public ResponseEntity<ApiResponse<List<AbilityHistoryResponse>>> getAbilityHistoriesBySkill(
            @PathVariable String userId,
            @PathVariable String skillId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        progressService.getAbilityHistoriesBySkill(
                                userId,
                                skillId)));
    }

    // ============================================================
    // SUMMARY
    // ============================================================

    @GetMapping("/users/{userId}/abilities/average")
    public ResponseEntity<ApiResponse<Double>> getAverageAbility(
            @PathVariable String userId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        progressService.getAverageAbility(userId)));
    }
}
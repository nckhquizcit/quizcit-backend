package vn.id.quiz.quizcitbackend.modules.progress.presentation.dto;

import java.time.LocalDateTime;

public record AbilityHistoryResponse(
        String id,
        String userId,
        String skillId,
        String skillName,
        double theta,
        double delta,
        String sourceAttemptId,
        LocalDateTime createdAt) {
}

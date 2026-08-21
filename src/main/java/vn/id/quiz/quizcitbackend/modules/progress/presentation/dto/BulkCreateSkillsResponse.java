package vn.id.quiz.quizcitbackend.modules.progress.presentation.dto;

import java.util.List;

public record BulkCreateSkillsResponse(
        int total,
        int successCount,
        int failureCount,
        List<BulkCreateSkillItemResponse> items) {
}

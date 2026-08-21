package vn.id.quiz.quizcitbackend.modules.progress.presentation.dto;

public record BulkCreateSkillItemResponse(
        String clientId,
        boolean success,
        SkillResponse skill,
        String message) {
}

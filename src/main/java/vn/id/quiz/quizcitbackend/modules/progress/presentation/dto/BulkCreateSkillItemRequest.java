package vn.id.quiz.quizcitbackend.modules.progress.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BulkCreateSkillItemRequest(
        @NotBlank(message = "clientId không được để trống")
        String clientId,
        @NotNull(message = "Dữ liệu kỹ năng không được để trống")
        @Valid
        SkillRequest skill) {
}

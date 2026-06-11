package vn.id.quiz.quizcitbackend.modules.content.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTheoryRequest(
        @NotBlank(message = "Nội dung lý thuyết không được để trống")
        String content
) {
}

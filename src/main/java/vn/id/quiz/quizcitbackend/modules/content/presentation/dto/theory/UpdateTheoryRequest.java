package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.theory;

import jakarta.validation.constraints.NotBlank;

public record UpdateTheoryRequest(
        @NotBlank(message = "Nội dung lý thuyết không được để trống")
        String content,
        String contentFormat
) {
    public UpdateTheoryRequest {
        contentFormat = "blocknote".equalsIgnoreCase(contentFormat) ? "blocknote" : "plain";
    }
}

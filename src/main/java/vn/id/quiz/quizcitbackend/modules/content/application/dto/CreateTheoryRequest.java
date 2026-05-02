package vn.id.quiz.quizcitbackend.modules.content.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTheoryRequest(
        @NotBlank(message="Một nội dung lý thuyết phải thuộc về một chương nào đó")
        String chapterId,
        @NotBlank(message = "Nội dung lý thuyết không được để trống")
        String content
) {
}

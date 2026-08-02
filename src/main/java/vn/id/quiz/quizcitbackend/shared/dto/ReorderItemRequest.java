package vn.id.quiz.quizcitbackend.shared.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ReorderItemRequest(
        @NotBlank String id,
        @Positive int displayOrder
) {}

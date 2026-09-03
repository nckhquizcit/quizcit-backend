package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BulkCreateQuestionItemRequest(
        @NotBlank(message = "clientId không được để trống")
        String clientId,
        @NotNull(message = "Dữ liệu câu hỏi không được để trống")
        @Valid
        CreateQuestionRequest question) {
}

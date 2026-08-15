package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question;

public record BulkCreateQuestionItemRequest(
        String clientId,
        CreateQuestionRequest question) {
}

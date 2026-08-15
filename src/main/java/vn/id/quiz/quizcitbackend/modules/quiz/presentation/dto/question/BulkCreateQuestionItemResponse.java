package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question;

public record BulkCreateQuestionItemResponse(
        String clientId,
        boolean success,
        String questionId,
        String message) {
}

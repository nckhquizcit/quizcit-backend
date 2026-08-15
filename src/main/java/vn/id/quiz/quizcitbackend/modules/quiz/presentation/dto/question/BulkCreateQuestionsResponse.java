package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question;

import java.util.List;

public record BulkCreateQuestionsResponse(
        int total,
        int successCount,
        int failureCount,
        List<BulkCreateQuestionItemResponse> items) {
}

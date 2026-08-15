package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record BulkCreateQuestionsRequest(
        @NotEmpty(message = "Danh sách câu hỏi không được để trống")
        @Size(max = 500, message = "Mỗi lần chỉ được import tối đa 500 câu hỏi")
        List<BulkCreateQuestionItemRequest> items) {
}

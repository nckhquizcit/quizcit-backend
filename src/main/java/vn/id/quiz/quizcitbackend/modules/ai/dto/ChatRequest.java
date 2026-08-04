package vn.id.quiz.quizcitbackend.modules.ai.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

/**
 * Request gọi AI Service sinh câu trả lời RAG cho chatbox lý thuyết.
 *
 * <p><b>Quan trọng:</b> {@code context} CHỈ được chứa các đoạn lý thuyết truy
 * hồi từ {@code theory_chunks} thuộc các chương sinh viên ĐÃ mở khóa — không
 * bao giờ được đưa nội dung câu hỏi/đáp án trắc nghiệm vào đây. Đây là lớp
 * bảo vệ kiến trúc quan trọng nhất của tính năng "không cung cấp đáp án".</p>
 */
public record ChatRequest(
        @NotBlank String question,
        List<String> context,
        List<ChatTurn> history,
        @JsonProperty("no_unlocked_context") boolean noUnlockedContext) {

    public ChatRequest {
        context = context == null ? List.of() : context;
        history = history == null ? List.of() : history;
    }
}

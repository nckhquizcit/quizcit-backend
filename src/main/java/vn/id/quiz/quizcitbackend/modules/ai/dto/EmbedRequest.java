package vn.id.quiz.quizcitbackend.modules.ai.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Request nhờ AI Service sinh embedding cho danh sách đoạn văn bản (dùng khi
 * index nội dung lý thuyết thành {@code theory_chunks}, hoặc khi embed câu
 * hỏi của sinh viên trước khi tìm kiếm pgvector).
 */
public record EmbedRequest(
        @NotNull @NotEmpty List<String> texts) {
}

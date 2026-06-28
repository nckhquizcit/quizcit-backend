package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO trả về thông tin skill gắn với một câu hỏi khi làm bài / xem chi tiết.
 *
 * <p><b>Task 1:</b> Enrich response skill thành object gồm {@code skillId},
 * {@code name}, {@code weight}, {@code primary} thay vì chỉ trả ID, để
 * frontend có thể hiển thị popup skill trên từng câu hỏi mà không cần gọi
 * thêm API skill list.</p>
 */
@Getter
@Setter
@Builder
public class QuestionSkillResponse {

    private String skillId;

    /**
     * Tên skill. Có thể null nếu skill đã bị xoá nhưng mapping còn sót lại.
     */
    private String name;

    private double weight;

    private boolean primary;
}

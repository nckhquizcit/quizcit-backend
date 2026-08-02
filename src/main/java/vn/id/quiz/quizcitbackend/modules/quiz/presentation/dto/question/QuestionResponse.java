package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.option.OptionResponse;

/**
 * Response trả về thông tin một câu hỏi.
 *
 * <p><b>Fix bug (Phase 1):</b> Đổi {@code explaination} -> {@code explanation}
 * cho đúng schema database.</p>
 *
 * <p><b>Phase 2 (P0):</b> Bổ sung trường {@code options} để frontend có
 * thể lấy danh sách lựa chọn kèm theo câu hỏi (dùng cho
 * {@code GET /api/quizzes/{quizId}/questions} và {@code GET /api/questions/{id}}).
 * Trường {@code correctOptionId} cho phép frontend xác định đáp án đúng
 * mà không cần gọi thêm API.</p>
 */
@Getter
@Setter
@Builder
public class QuestionResponse {

    private String questionId;

    private String subjectId;

    private String chapterId;

    private String content;

    private String explanation;

    private String contentFormat;

    private double difficultyB;

    /**
     * Danh sách lựa chọn của câu hỏi. Có thể null khi câu hỏi chưa gán option.
     */
    private List<OptionResponse> options;

    /**
     * ID của lựa chọn đúng (chỉ trả về khi người gọi có quyền xem đáp án,
     * ví dụ: khi xem chi tiết câu hỏi ngoài ngữ cảnh làm bài).
     */
    private String correctOptionId;

    /**
     * Danh sách skill gắn với câu hỏi (đã enrich tên skill) để frontend hiển
     * thị popup skill. Xem {@link QuestionSkillResponse}.
     */
    private List<QuestionSkillResponse> skills;
}


package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.option;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO trả về thông tin một lựa chọn (option) của câu hỏi.
 *
 * <p>Trước đây QuestionResponse không có field options, khiến frontend
 * không lấy được đáp án cho câu hỏi trắc nghiệm. DTO này được sử dụng
 * trong QuestionResponse.options.</p>
 */
@Getter
@Setter
@Builder
public class OptionResponse {

    private String optionId;

    private String content;

    /**
     * Cờ đánh dấu đáp án đúng. Tuỳ theo ngữ cảnh (làm bài / xem chi tiết)
     * mà service có thể ẩn/hiện cờ này.
     */
    private boolean corrected;
}

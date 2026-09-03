package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.option.CreateOptionRequest;

/**
 * Request body cho {@code PUT /api/questions/{id}}.
 *
 * <p><b>Fix bug:</b> Trước đây field là {@code explaination}. Đổi sang
 * {@code explanation}.</p>
 *
 * <p><b>Phase 2 (P1):</b> Nâng cấp API cập nhật câu hỏi để đồng thời
 * thay thế danh sách {@code options} và {@code skills} (skill mapping)
 * trong cùng một request.</p>
 */
@Getter
@Setter
public class UpdateQuestionRequest {

    private String subjectId;

    private String chapterId;

    /**
     * {@code updateQuestion} hiện luôn ghi đè trực tiếp {@code content} lên
     * entity (không coi {@code null} là "giữ nguyên" như với {@code options}
     * / {@code skills}), nên bắt buộc không được để trống giống request tạo mới.
     */
    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    @Size(max = 200000, message = "Nội dung câu hỏi quá dài")
    private String content;

    @Size(max = 200000, message = "Giải thích quá dài")
    private String explanation;

    private String contentFormat;

    private double difficultyB;

    /**
     * Nếu khác null, danh sách option của câu hỏi sẽ được thay thế hoàn toàn
     * bằng danh sách này (xoá option cũ, insert lại). Cho phép null để giữ
     * nguyên option hiện tại.
     */
    @Valid
    private List<CreateOptionRequest> options;

    /**
     * Nếu khác null, ánh xạ skill của câu hỏi sẽ được thay thế bằng danh sách
     * này. Cho phép null để giữ nguyên.
     */
    @Valid
    private List<@Valid QuestionSkillMappingRequest> skills;

    public String getContentFormat() {
        return "blocknote".equalsIgnoreCase(contentFormat) ? "blocknote" : "plain";
    }
}

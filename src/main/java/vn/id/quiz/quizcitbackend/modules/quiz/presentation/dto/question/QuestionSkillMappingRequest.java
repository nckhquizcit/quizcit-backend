package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO mapping giữa câu hỏi và kỹ năng (skill). Dùng cho Phase 2 (P1):
 * API quản lý kỹ năng / IRT - cho phép gắn / cập nhật trọng số kỹ năng
 * cho một câu hỏi trắc nghiệm.
 */
@Getter
@Setter
public class QuestionSkillMappingRequest {

    @NotBlank(message = "ID kỹ năng không được để trống")
    private String skillId;

    @Positive(message = "Trọng số kỹ năng phải lớn hơn 0")
    private Double weight;

    private boolean primary;
}

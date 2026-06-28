package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question;

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

    private String skillId;

    private Double weight;

    private boolean primary;
}

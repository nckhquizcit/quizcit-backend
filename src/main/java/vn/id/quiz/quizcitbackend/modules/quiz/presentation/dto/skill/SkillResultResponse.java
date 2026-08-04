package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.skill;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SkillResultResponse {

    private String skillId;

    private String skillName;

    private double theta;

    // MỚI: mức thay đổi theta so với trước lần làm bài này (âm = giảm,
    // dương = tăng). Dùng để FE hiển thị mũi tên tăng/giảm và để AI sinh lời
    // khuyên (xem AIService.generateAdvice).
    private double delta;

}

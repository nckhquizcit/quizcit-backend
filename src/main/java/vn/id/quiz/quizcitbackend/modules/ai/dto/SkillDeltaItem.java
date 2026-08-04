package vn.id.quiz.quizcitbackend.modules.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Dữ liệu thay đổi năng lực 1 kỹ năng sau 1 lần nộp bài, dùng làm input cho
 * AI Service sinh lời khuyên cải thiện.
 */
public record SkillDeltaItem(
        @JsonProperty("skill_name") String skillName,
        @JsonProperty("old_theta") double oldTheta,
        @JsonProperty("new_theta") double newTheta,
        double delta) {
}

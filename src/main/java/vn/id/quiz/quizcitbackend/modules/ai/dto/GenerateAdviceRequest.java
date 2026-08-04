package vn.id.quiz.quizcitbackend.modules.ai.dto;

import java.util.List;

public record GenerateAdviceRequest(List<SkillDeltaItem> skills) {

    public GenerateAdviceRequest {
        skills = skills == null ? List.of() : skills;
    }
}

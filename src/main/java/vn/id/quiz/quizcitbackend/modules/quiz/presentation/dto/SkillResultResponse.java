package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SkillResultResponse {

    private String skillId;

    private String skillName;

    private double theta;

    private Double standardError;

}

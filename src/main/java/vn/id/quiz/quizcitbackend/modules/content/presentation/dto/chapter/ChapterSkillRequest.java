package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChapterSkillRequest(
        @NotBlank String skillId,
        @NotNull Double requiredTheta
) {}

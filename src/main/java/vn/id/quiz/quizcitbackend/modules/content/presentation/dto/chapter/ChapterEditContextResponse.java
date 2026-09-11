package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter;

import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.theory.TheoryResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.SkillResponse;

import java.util.List;

public record ChapterEditContextResponse(
        ChapterResponse chapter,
        String subjectId,
        String subjectName,
        int siblingChaptersCount,
        List<ChapterResponse> siblingChapters,
        List<SkillResponse> availableSkills,
        List<TheoryResponse> theories
) {
    public ChapterEditContextResponse {
        if (siblingChapters == null) {
            siblingChapters = List.of();
        }
        if (availableSkills == null) {
            availableSkills = List.of();
        }
        if (theories == null) {
            theories = List.of();
        }
    }
}

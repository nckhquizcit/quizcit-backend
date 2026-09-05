package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter;

public record ChapterSkillResponse(
        String skillId,
        String name,
        double requiredTheta,
        Double currentTheta
) {
    public ChapterSkillResponse(String skillId, String name, double requiredTheta) {
        this(skillId, name, requiredTheta, null);
    }
}

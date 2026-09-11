package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter;

public record ChapterSkillResponse(
        String skillId,
        String name,
        double requiredTheta,
        Double currentTheta,
        boolean met
) {
    public ChapterSkillResponse(String skillId, String name, double requiredTheta, Double currentTheta) {
        this(skillId, name, requiredTheta, currentTheta, currentTheta != null && currentTheta >= requiredTheta);
    }

    public ChapterSkillResponse(String skillId, String name, double requiredTheta) {
        this(skillId, name, requiredTheta, null, false);
    }
}

package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.ChapterSkillResponse;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterStudyOverviewResponse {
    private String chapterId;
    private String subjectId;
    private String name;
    private int displayOrder;
    private boolean unlocked;
    private List<TheoryStudyOverviewResponse> theories;
    private List<QuizStudyOverviewResponse> quizzes;
    private List<ChapterSkillResponse> requiredSkills;

    public List<ChapterSkillResponse> getSkills() {
        return requiredSkills;
    }
}

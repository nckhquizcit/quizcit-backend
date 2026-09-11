package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizStudyOverviewResponse {
    private String quizId;
    private String chapterId;
    private String name;
    private int totalQuestions;
    private int durationMinutes;

    public int getTotalQuestion() {
        return totalQuestions;
    }

    public int getDuration() {
        return durationMinutes;
    }
}

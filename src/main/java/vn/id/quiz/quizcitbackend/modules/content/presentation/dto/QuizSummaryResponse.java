package vn.id.quiz.quizcitbackend.modules.content.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import java.time.ZonedDateTime;

/**
 * Phase 2 (P1) - Response rút gọn cho {@code Quiz}.
 *
 * <p>Được trả về khi gọi {@code GET /api/subjects/{subjectId}/quizzes} để
 * liệt kê các đề thi thuộc một môn học mà không cần đính kèm toàn bộ câu hỏi.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSummaryResponse {
    private String quizId;
    private String name;
    private String subjectId;
    private String chapterId;
    private Integer totalQuestion;
    private Integer duration;
    private String type;
    private Integer displayOrder;
    private ZonedDateTime createdAt;

    public static QuizSummaryResponse from(Quiz quiz) {
        if (quiz == null) {
            return null;
        }
        return QuizSummaryResponse.builder()
                .quizId(quiz.getQuizId())
                .name(quiz.getName())
                .subjectId(quiz.getSubjectId())
                .chapterId(quiz.getChapterId())
                .totalQuestion(quiz.getTotalQuestion())
                .duration(quiz.getDuration())
                .type(quiz.getType() != null ? quiz.getType().name() : null)
                .displayOrder(quiz.getDisplayOrder())
                .createdAt(quiz.getCreatedAt())
                .build();
    }
}

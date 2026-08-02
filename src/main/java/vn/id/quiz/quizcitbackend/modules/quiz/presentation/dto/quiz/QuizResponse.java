package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz;

import lombok.Builder;
import lombok.Getter;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizType;
import java.time.ZonedDateTime;

@Getter
@Builder
public class QuizResponse {

    private String quizId;

    private String subjectId;

    private String chapterId;

    private String name;

    private QuizType type;

    private int totalQuestion;

    private int duration;

    private int displayOrder;

    private ZonedDateTime createdAt;
}

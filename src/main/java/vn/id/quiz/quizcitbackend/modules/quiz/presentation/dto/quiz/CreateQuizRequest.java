package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizType;

@Getter
@Setter
public class CreateQuizRequest {

    private String subjectId;

    private String chapterId;

    private String name;

    private int totalQuestion;

    private int duration;

    private QuizType type;

    private List<String> questionIds;

    private Integer displayOrder;
}

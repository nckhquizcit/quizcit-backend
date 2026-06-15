package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto;

import lombok.Builder;
import lombok.Getter;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizType;

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
}
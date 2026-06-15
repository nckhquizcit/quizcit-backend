package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuestionResponse {

    private String questionId;

    private String subjectId;

    private String chapterId;

    private String content;

    private String explaination;

    private double difficultyB;

    private boolean active;
}

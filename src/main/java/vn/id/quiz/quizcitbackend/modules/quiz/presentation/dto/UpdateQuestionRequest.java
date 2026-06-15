package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateQuestionRequest {

    private String content;

    private String explaination;

    private double difficultyB;
}

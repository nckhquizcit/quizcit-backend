package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateQuizRequest {

    private String name;

    private Integer duration;

    private String chapterId;
}
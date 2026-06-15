package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateQuestionRequest {

    private String subjectId;

    private String chapterId;

    private String content;

    private String explaination;

    private double difficultyB;

    private List<CreateOptionRequest> options;
}
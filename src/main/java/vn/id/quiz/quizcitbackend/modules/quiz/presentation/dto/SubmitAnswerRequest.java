package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitAnswerRequest {

    private String questionId;

    private String optionId;
}

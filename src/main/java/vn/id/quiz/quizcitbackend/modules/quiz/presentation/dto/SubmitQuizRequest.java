package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitQuizRequest {

    private String attemptId;

    private List<SubmitAnswerRequest> answers;
}
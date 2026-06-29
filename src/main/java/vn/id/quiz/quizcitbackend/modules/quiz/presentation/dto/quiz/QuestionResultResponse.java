package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.option.OptionResponse;

@Getter
@Builder
public class QuestionResultResponse {

    private String questionId;

    private int questionNumber;

    private String content;

    private String explanation;

    private String selectedOptionId;

    private String correctOptionId;

    private boolean correct;

    private List<OptionResponse> options;
}

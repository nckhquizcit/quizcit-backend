package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOptionRequest {

    private String content;

    private boolean corrected;
}
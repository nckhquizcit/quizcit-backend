package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.option;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOptionRequest {

    @Size(max = 200000, message = "Nội dung đáp án quá dài")
    private String content;

    private boolean corrected;
}
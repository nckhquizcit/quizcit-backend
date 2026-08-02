package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class UpdateQuizRequest {

    private String name;

    private Integer duration;

    private String chapterId;

    private Integer displayOrder;

    private List<String> questionIds;
}

package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateQuizRequest {

    @NotBlank(message = "Tên bài kiểm tra không được để trống")
    @Size(max = 200, message = "Tên bài kiểm tra không được vượt quá 200 ký tự")
    private String name;

    @NotNull(message = "Thời gian làm bài không được để trống")
    @Min(value = 1, message = "Thời gian làm bài phải lớn hơn 0 phút")
    private Integer duration;

    private String chapterId;

    private Integer displayOrder;

    private List<String> questionIds;
}

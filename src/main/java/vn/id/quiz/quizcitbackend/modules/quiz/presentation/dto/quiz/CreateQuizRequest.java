package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizType;

@Getter
@Setter
public class CreateQuizRequest {

    @NotBlank(message = "Mã môn học không được để trống")
    private String subjectId;

    @NotBlank(message = "Mã chương không được để trống")
    private String chapterId;

    @NotBlank(message = "Tên bài kiểm tra không được để trống")
    @Size(max = 200, message = "Tên bài kiểm tra không được vượt quá 200 ký tự")
    private String name;

    @Min(value = 0, message = "Số lượng câu hỏi không hợp lệ")
    private int totalQuestion;

    @Min(value = 1, message = "Thời gian làm bài phải lớn hơn 0 phút")
    private int duration;

    @NotNull(message = "Loại bài kiểm tra không được để trống")
    private QuizType type;

    private List<String> questionIds;

    private Integer displayOrder;
}

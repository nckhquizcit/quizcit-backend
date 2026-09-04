package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request body cho API gắn nhanh môn học và chương hàng loạt:
 * {@code PATCH /api/questions/placement}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkAssignQuestionPlacementRequest {

    @NotEmpty(message = "Danh sách ID câu hỏi không được để trống")
    private List<String> questionIds;

    @NotBlank(message = "Mã học phần không được để trống")
    private String subjectId;

    @NotBlank(message = "Mã chương học không được để trống")
    private String chapterId;
}

package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body cho API gắn nhanh môn học và chương vào câu hỏi:
 * {@code PATCH /api/questions/{questionId}/placement}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignQuestionPlacementRequest {

    @NotBlank(message = "Mã học phần không được để trống")
    private String subjectId;

    @NotBlank(message = "Mã chương học không được để trống")
    private String chapterId;
}

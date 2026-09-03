package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

public record CreateSubjectRequest(
        @NotBlank(message = ErrorMessages.COURSE_CODE_REQUIRED)
        @Size(min = 2, max = 20, message = ErrorMessages.COURSE_CODE_LENGTH)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = ErrorMessages.COURSE_CODE_INVALID)
        String courseCode,
        @NotBlank(message="Tên học phần không được để trống")
        @Size(max = 200, message = "Tên học phần không được vượt quá 200 ký tự")
        String name,
        String description,
        Integer displayOrder,
        @Size(max = 500, message = "URL hình ảnh không được vượt quá 500 ký tự")
        String imageUrl
) {
}

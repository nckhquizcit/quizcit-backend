package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSubjectRequest (
        @NotBlank(message = "Mã học phần không được để trống")
        @Size(max = 20, message = "Mã học phần không được vượt quá 20 ký tự")
        String courseCode,
        @NotBlank(message="Tên học phần không được để trống")
        @Size(max = 200, message = "Tên học phần không được vượt quá 200 ký tự")
        String name,
        String description,
        boolean visible
) {
}

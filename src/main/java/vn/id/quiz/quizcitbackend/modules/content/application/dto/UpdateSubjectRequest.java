package vn.id.quiz.quizcitbackend.modules.content.application.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateSubjectRequest (
        @NotBlank(message = "Mã học phần không được để trống")
        String courseCode,
        @NotBlank(message="Tên học phần không được để trống")
        String name,
        String description
) {
}

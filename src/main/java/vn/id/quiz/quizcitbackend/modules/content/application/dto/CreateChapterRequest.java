package vn.id.quiz.quizcitbackend.modules.content.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateChapterRequest (
        @NotBlank(message="Một chương phải thuộc vào một học phần nào đó")
        String subjectId,
        @NotBlank(message="Tên chương không được để trống")
        @Size(max=200, message="Tên chương không được vượt quá 200 ký tự")
        String name
){
}

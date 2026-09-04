package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject;

import jakarta.validation.constraints.NotBlank;

public record ChangeSubjectRequest (

        @NotBlank(message = "Mã học phần nguồn không được để trống")
        String sourceSubject,
        @NotBlank(message = "Mã học phần đích không được để trống")
        String targetSubject
){
}

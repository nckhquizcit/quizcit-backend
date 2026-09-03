package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject;

import jakarta.validation.constraints.NotBlank;

public record ChangeSubjectRequest (

        @NotBlank(message = "Mã môn học nguồn không được để trống")
        String sourceSubject,
        @NotBlank(message = "Mã môn học đích không được để trống")
        String targetSubject
){
}

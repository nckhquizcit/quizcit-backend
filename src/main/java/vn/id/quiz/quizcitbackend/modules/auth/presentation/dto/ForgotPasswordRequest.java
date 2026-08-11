package vn.id.quiz.quizcitbackend.modules.auth.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

public record ForgotPasswordRequest(
        @NotBlank(message = ErrorMessages.EMAIL_REQUIRED)
        @Email(message = ErrorMessages.EMAIL_INVALID)
        @Size(max = 100, message = ErrorMessages.EMAIL_LENGTH)
        String email
) {
}

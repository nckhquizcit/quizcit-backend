package vn.id.quiz.quizcitbackend.modules.auth.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

public record RegisterRequest(
        @NotBlank(message = ErrorMessages.USERNAME_REQUIRED)
        @Size(min = 3, max = 50, message = ErrorMessages.USERNAME_LENGTH)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = ErrorMessages.USERNAME_INVALID)
        String username,

        @NotBlank(message = ErrorMessages.PASSWORD_REQUIRED)
        @Size(min = 6, max = 255, message = ErrorMessages.PASSWORD_LENGTH)
        String password,

        @NotBlank(message = ErrorMessages.EMAIL_REQUIRED)
        @Email(message = ErrorMessages.EMAIL_INVALID)
        @Size(max = 100, message = ErrorMessages.EMAIL_LENGTH)
        String email,

        @Size(max = 100, message = ErrorMessages.DISPLAY_NAME_LENGTH)
        String displayName
) {
}

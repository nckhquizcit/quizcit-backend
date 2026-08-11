package vn.id.quiz.quizcitbackend.modules.auth.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

public record ResetPasswordRequest(
        @NotBlank(message = "Token xác nhận không được để trống")
        String confirmedToken,

        @NotBlank(message = ErrorMessages.PASSWORD_REQUIRED)
        @Size(min = 6, max = 255, message = ErrorMessages.PASSWORD_LENGTH)
        String newPassword
) {
}

package vn.id.quiz.quizcitbackend.modules.auth.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

public record RegisterRequest(
        @NotBlank(message = "Tên đăng nhập không được để trống")
        @Size(max = 50, message = "Tên đăng nhập không được vượt quá 50 ký tự")
        String username,

        @NotBlank(message = ErrorMessages.PASSWORD_REQUIRED)
        @Size(min = 6, max = 255, message = ErrorMessages.PASSWORD_LENGTH)
        String password,

        @NotBlank(message = ErrorMessages.EMAIL_REQUIRED)
        @Email(message = ErrorMessages.EMAIL_INVALID)
        @Size(max = 100, message = ErrorMessages.EMAIL_LENGTH)
        String email,

        @Size(max = 100, message = "Tên hiển thị không được vượt quá 100 ký tự")
        String displayName
) {
}

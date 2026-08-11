package vn.id.quiz.quizcitbackend.modules.auth.presentation.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

public record LoginRequest(
        @JsonAlias({"username", "email"})
        @NotBlank(message = "Tên đăng nhập hoặc email không được để trống")
        @Size(max = 100, message = "Tên đăng nhập hoặc email không được vượt quá 100 ký tự")
        String identifier,

        @NotBlank(message = ErrorMessages.PASSWORD_REQUIRED)
        @Size(min = 6, max = 255, message = ErrorMessages.PASSWORD_LENGTH)
        String password
) {
}

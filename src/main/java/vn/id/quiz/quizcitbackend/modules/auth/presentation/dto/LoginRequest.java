package vn.id.quiz.quizcitbackend.modules.auth.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Tên đăng nhập hoặc email không được để trống")
        @Size(max = 100, message = "Tên đăng nhập hoặc email không được vượt quá 100 ký tự")
        String identifier,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, max = 255, message = "Mật khẩu phải từ 6 đến 255 ký tự")
        String password
) {
}

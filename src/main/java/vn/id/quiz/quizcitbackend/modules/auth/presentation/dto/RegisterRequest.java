package vn.id.quiz.quizcitbackend.modules.auth.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Tên đăng nhập không được để trống")
        @Size(max = 50, message = "Tên đăng nhập không được vượt quá 50 ký tự")
        String username,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, max = 255, message = "Mật khẩu phải từ 6 đến 255 ký tự")
        String password,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
        String email,

        @Size(max = 100, message = "Tên hiển thị không được vượt quá 100 ký tự")
        String displayName
) {
}

package vn.id.quiz.quizcitbackend.modules.user.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminResetPasswordRequest(
        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(min = 6, max = 255, message = "Mật khẩu mới phải từ 6 đến 255 ký tự")
        String newPassword
) {
}

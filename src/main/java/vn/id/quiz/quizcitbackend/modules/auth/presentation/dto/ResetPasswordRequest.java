package vn.id.quiz.quizcitbackend.modules.auth.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Token xác nhận không được để trống")
        String confirmedToken,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, max = 255, message = "Mật khẩu phải từ 6 đến 255 ký tự")
        String newPassword
) {
}

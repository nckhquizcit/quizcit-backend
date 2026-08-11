package vn.id.quiz.quizcitbackend.modules.auth.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record VerifyResetOtpRequest(
        @NotBlank(message = "Token đặt lại mật khẩu không được để trống")
        String resetToken,

        @NotBlank(message = "Mã xác thực không được để trống")
        @Size(min = 6, max = 6, message = "Mã xác thực phải gồm 6 chữ số")
        @Pattern(regexp = "\\d{6}", message = "Mã xác thực phải gồm 6 chữ số")
        String otp
) {
}

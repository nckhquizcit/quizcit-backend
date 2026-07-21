package vn.id.quiz.quizcitbackend.modules.auth.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyRequest(
        @NotBlank(message = "Token xác thực không được để trống")
        String verificationToken,

        @NotBlank(message = "Mã xác thực không được để trống")
        @Size(min = 6, max = 6, message = "Mã xác thực phải gồm 6 chữ số")
        String otp
) {
}

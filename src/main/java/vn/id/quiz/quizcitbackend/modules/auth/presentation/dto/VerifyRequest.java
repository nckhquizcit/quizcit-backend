package vn.id.quiz.quizcitbackend.modules.auth.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyRequest(
        @NotBlank(message = "Token xac thuc khong duoc de trong")
        String verificationToken,

        @NotBlank(message = "Ma xac thuc khong duoc de trong")
        @Size(min = 6, max = 6, message = "Ma xac thuc phai gom 6 chu so")
        String otp
) {
}

package vn.id.quiz.quizcitbackend.modules.user.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerifyOldEmailOtpRequest(
        @NotBlank(message = "Mã yêu cầu đổi email không được để trống") String challengeId,
        @NotBlank(message = "Mã xác thực không được để trống")
        @Pattern(regexp = "\\d{6}", message = "Mã xác thực phải gồm 6 chữ số") String otp,
        @NotBlank(message = "Email mới không được để trống")
        @Email(message = "Email mới không hợp lệ")
        @Size(max = 100, message = "Email mới không được vượt quá 100 ký tự") String newEmail
) {}

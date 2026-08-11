package vn.id.quiz.quizcitbackend.modules.user.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyEmailChangeOtpRequest(
        @NotBlank(message = "Mã yêu cầu đổi email không được để trống") String challengeId,
        @NotBlank(message = "Mã xác thực không được để trống")
        @Pattern(regexp = "\\d{6}", message = "Mã xác thực phải gồm 6 chữ số") String otp
) {}

package vn.id.quiz.quizcitbackend.modules.auth.presentation.dto;

public record ForgotPasswordResponse(
        String email,
        String resetToken
) {
}

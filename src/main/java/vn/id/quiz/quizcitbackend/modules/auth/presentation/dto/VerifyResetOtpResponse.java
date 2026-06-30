package vn.id.quiz.quizcitbackend.modules.auth.presentation.dto;

public record VerifyResetOtpResponse(
        String email,
        String confirmedToken
) {
}

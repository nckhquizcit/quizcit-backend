package vn.id.quiz.quizcitbackend.modules.auth.presentation.dto;

public record RegisterResponse(
        String email,
        String verificationToken
) {
}

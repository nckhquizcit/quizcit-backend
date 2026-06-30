package vn.id.quiz.quizcitbackend.modules.auth.service;

public record VerificationClaims(String email, String otpHash) {
}

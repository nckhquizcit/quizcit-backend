package vn.id.quiz.quizcitbackend.modules.auth.service;

import java.time.ZonedDateTime;

public record RefreshTokenDetails(String token, ZonedDateTime expiresAt) {
}

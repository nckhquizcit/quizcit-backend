package vn.id.quiz.quizcitbackend.modules.auth.service;

import java.time.ZonedDateTime;

public record TokenDetails(
        String accessToken,
        String tokenType,
        ZonedDateTime issuedAt,
        ZonedDateTime expiresAt
) {
}

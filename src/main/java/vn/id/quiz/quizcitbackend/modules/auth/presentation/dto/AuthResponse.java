package vn.id.quiz.quizcitbackend.modules.auth.presentation.dto;

import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserStatus;

import java.time.ZonedDateTime;

public record AuthResponse(
        String accessToken,
        String tokenType,
        ZonedDateTime expiresAt,
        String refreshToken,
        ZonedDateTime refreshTokenExpiresAt,
        String userId,
        String username,
        String email,
        String displayName,
        String avatarUrl,
        UserRole role,
        UserStatus status,
        ZonedDateTime createdAt
) {
    public AuthResponse(
            String accessToken,
            String tokenType,
            ZonedDateTime expiresAt,
            String refreshToken,
            ZonedDateTime refreshTokenExpiresAt,
            String userId,
            String username,
            String email,
            String displayName,
            UserRole role,
            UserStatus status,
            ZonedDateTime createdAt
    ) {
        this(accessToken, tokenType, expiresAt, refreshToken, refreshTokenExpiresAt,
                userId, username, email, displayName, null, role, status, createdAt);
    }
}

package vn.id.quiz.quizcitbackend.modules.auth.presentation.dto;

import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserStatus;

import java.time.ZonedDateTime;

public record AuthResponse(
        String accessToken,
        String tokenType,
        ZonedDateTime expiresAt,
        String userId,
        String username,
        String email,
        String displayName,
        UserRole role,
        UserStatus status,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
}

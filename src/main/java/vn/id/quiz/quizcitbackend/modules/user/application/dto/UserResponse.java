package vn.id.quiz.quizcitbackend.modules.user.application.dto;

import vn.id.quiz.quizcitbackend.modules.user.domain.entity.UserRole;
import vn.id.quiz.quizcitbackend.modules.user.domain.entity.UserStatus;

import java.time.ZonedDateTime;

public record UserResponse(
        String userId,
        String username,
        String email,
        String displayName,
        UserRole role,
        UserStatus status,
        ZonedDateTime createdAt
) {
    public UserResponse {
        if (createdAt == null) {
            createdAt = ZonedDateTime.now();
        }
    }
}

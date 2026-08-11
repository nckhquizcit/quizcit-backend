package vn.id.quiz.quizcitbackend.modules.auth.service;

import org.springframework.stereotype.Component;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.AuthResponse;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;

@Component
public class AuthMapper {
    public AuthResponse toAuthResponse(
            User user, UserRole role, TokenDetails tokenDetails, RefreshTokenDetails refreshTokenDetails) {
        if (user == null) {
            return null;
        }

        return new AuthResponse(
                tokenDetails.accessToken(),
                tokenDetails.tokenType(),
                tokenDetails.expiresAt(),
                refreshTokenDetails.token(),
                refreshTokenDetails.expiresAt(),
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                role,
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}

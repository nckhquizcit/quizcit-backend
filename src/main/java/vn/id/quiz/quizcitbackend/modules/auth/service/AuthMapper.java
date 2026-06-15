package vn.id.quiz.quizcitbackend.modules.auth.service;

import org.mapstruct.Mapper;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.AuthResponse;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    default AuthResponse toAuthResponse(User user, UserRole role, TokenDetails tokenDetails) {
        if (user == null) {
            return null;
        }

        return new AuthResponse(
                tokenDetails.accessToken(),
                tokenDetails.tokenType(),
                tokenDetails.expiresAt(),
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                role,
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}

package vn.id.quiz.quizcitbackend.modules.user.service;

import org.mapstruct.Mapper;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UserResponse;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;

@Mapper(componentModel = "spring")
public interface UserMapper {
    // UserResponse can role, nhung role nam o bang user_roles nen service truyen vao rieng.
    default UserResponse toUserResponse(User entity, UserRole role) {
        if (entity == null) {
            return null;
        }

        return new UserResponse(
                entity.getUserId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getDisplayName(),
                role,
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

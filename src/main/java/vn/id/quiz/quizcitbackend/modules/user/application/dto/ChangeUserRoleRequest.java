package vn.id.quiz.quizcitbackend.modules.user.application.dto;

import jakarta.validation.constraints.NotNull;
import vn.id.quiz.quizcitbackend.modules.user.domain.entity.UserRole;

public record ChangeUserRoleRequest(
        @NotNull(message = "Vai trò người dùng không được để trống")
        UserRole role
) {
}

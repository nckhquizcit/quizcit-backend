package vn.id.quiz.quizcitbackend.modules.user.presentation.dto;

import jakarta.validation.constraints.NotNull;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;

public record ChangeUserRoleRequest(
        @NotNull(message = "Vai trò người dùng không được để trống")
        UserRole role
) {
}

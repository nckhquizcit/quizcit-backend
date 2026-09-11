package vn.id.quiz.quizcitbackend.modules.user.presentation.dto;

import vn.id.quiz.quizcitbackend.modules.user.presentation.model.Permission;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.Role;

import java.util.List;

public record AccessMatrixResponse(
        List<Role> roles,
        List<Permission> permissions
) {
    public AccessMatrixResponse {
        if (roles == null) {
            roles = List.of();
        }
        if (permissions == null) {
            permissions = List.of();
        }
    }
}

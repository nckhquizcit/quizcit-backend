package vn.id.quiz.quizcitbackend.modules.user.service;

import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.ChangePasswordRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.AdminResetPasswordRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.ChangeUserRoleRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UpdateUserProfileRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UpdateUserStatusRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UserResponse;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserStatus;

public interface UserUseCase {
    List<UserResponse> getAllUsers();
    default Page<UserResponse> getAllUsers(Pageable pageable) {
        return getAllUsers(null, null, null, null, pageable);
    }
    Page<UserResponse> getAllUsers(String search, UserRole role, UserStatus status, String currentUserId, Pageable pageable);

    UserResponse getUserById(String userId);

    UserResponse updateUserProfile(String userId, UpdateUserProfileRequest request);

    void changePassword(String userId, ChangePasswordRequest request);
    void resetPasswordByAdmin(String userId, AdminResetPasswordRequest request);

    void deleteUser(String actorUserId, String userId);
    void hardDeleteUser(String actorUserId, String userId);

    // ===== Phase 2 (P1): Admin endpoints =====

    UserResponse updateUserStatus(String actorUserId, String userId, UpdateUserStatusRequest request);

    UserResponse changeUserRole(String actorUserId, String userId, ChangeUserRoleRequest request);

    // ===== Role & Permission Management =====
    List<vn.id.quiz.quizcitbackend.modules.user.presentation.model.Role> getAllRoles();
    void deleteRole(String roleId);
    void hardDeleteRole(String roleId);

    List<vn.id.quiz.quizcitbackend.modules.user.presentation.model.Permission> getAllPermissions();
    void deletePermission(String permissionId);
    void hardDeletePermission(String permissionId);
}

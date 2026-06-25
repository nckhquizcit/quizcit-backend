package vn.id.quiz.quizcitbackend.modules.user.service;

import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.ChangePasswordRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.ChangeUserRoleRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UpdateUserProfileRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UpdateUserStatusRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UserResponse;

import java.util.List;

public interface UserUseCase {
    List<UserResponse> getAllUsers();

    UserResponse getUserById(String userId);

    UserResponse updateUserProfile(String userId, UpdateUserProfileRequest request);

    void changePassword(String userId, ChangePasswordRequest request);

    void deleteUser(String userId);

    // ===== Phase 2 (P1): Admin endpoints =====

    UserResponse updateUserStatus(String userId, UpdateUserStatusRequest request);

    UserResponse changeUserRole(String userId, ChangeUserRoleRequest request);
}

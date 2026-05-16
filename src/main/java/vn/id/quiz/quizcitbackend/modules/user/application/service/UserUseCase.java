package vn.id.quiz.quizcitbackend.modules.user.application.service;

import vn.id.quiz.quizcitbackend.modules.user.application.dto.ChangeUserRoleRequest;
import vn.id.quiz.quizcitbackend.modules.user.application.dto.ChangeUserStatusRequest;
import vn.id.quiz.quizcitbackend.modules.user.application.dto.CreateUserRequest;
import vn.id.quiz.quizcitbackend.modules.user.application.dto.UpdateUserProfileRequest;
import vn.id.quiz.quizcitbackend.modules.user.application.dto.UserResponse;

import java.util.List;

public interface UserUseCase {
    List<UserResponse> getAllUsers();
    UserResponse getUserById(String userId);
    UserResponse createUser(CreateUserRequest request);
    void updateUserProfile(String userId, UpdateUserProfileRequest request);
    void changeUserRole(String userId, ChangeUserRoleRequest request);
    void changeUserStatus(String userId, ChangeUserStatusRequest request);
    void banUser(String userId);
    void verifyUser(String userId);
    void deleteUser(String userId);
}

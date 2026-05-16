package vn.id.quiz.quizcitbackend.modules.user.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.id.quiz.quizcitbackend.modules.user.application.dto.ChangeUserRoleRequest;
import vn.id.quiz.quizcitbackend.modules.user.application.dto.ChangeUserStatusRequest;
import vn.id.quiz.quizcitbackend.modules.user.application.dto.CreateUserRequest;
import vn.id.quiz.quizcitbackend.modules.user.application.dto.UpdateUserProfileRequest;
import vn.id.quiz.quizcitbackend.modules.user.application.dto.UserResponse;
import vn.id.quiz.quizcitbackend.modules.user.application.service.UserUseCase;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserUseCase userUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> responses = userUseCase.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String userId) {
        UserResponse response = userUseCase.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        UserResponse response = userUseCase.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> updateUserProfile(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        userUseCase.updateUserProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật người dùng thành công", null));
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<Void>> changeUserRole(
            @PathVariable String userId,
            @Valid @RequestBody ChangeUserRoleRequest request
    ) {
        userUseCase.changeUserRole(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật vai trò người dùng thành công", null));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<Void>> changeUserStatus(
            @PathVariable String userId,
            @Valid @RequestBody ChangeUserStatusRequest request
    ) {
        userUseCase.changeUserStatus(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái người dùng thành công", null));
    }

    @PatchMapping("/{userId}/ban")
    public ResponseEntity<ApiResponse<Void>> banUser(@PathVariable String userId) {
        userUseCase.banUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Đã khóa người dùng thành công", null));
    }

    @PatchMapping("/{userId}/verify")
    public ResponseEntity<ApiResponse<Void>> verifyUser(@PathVariable String userId) {
        userUseCase.verifyUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Đã xác minh người dùng thành công", null));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String userId) {
        userUseCase.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success("Đã xóa người dùng thành công", null));
    }
}

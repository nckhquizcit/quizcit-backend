package vn.id.quiz.quizcitbackend.modules.user.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.ChangePasswordRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.ChangeUserRoleRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UpdateUserProfileRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UpdateUserStatusRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UserResponse;
import vn.id.quiz.quizcitbackend.modules.user.service.UserUseCase;
import vn.id.quiz.quizcitbackend.shared.security.CustomUserPrincipal;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserUseCase userUseCase;

    // Lay ho so cua user dang dang nhap.
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        UserResponse response = userUseCase.getUserById(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // USER tu cap nhat displayName/email cua chinh minh.
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUserProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        UserResponse response = userUseCase.updateUserProfile(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Cap nhat ho so thanh cong", response));
    }

    // USER tu doi mat khau cua chinh minh.
    @PatchMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changeCurrentUserPassword(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userUseCase.changePassword(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Cap nhat mat khau thanh cong", null));
    }

    // =========================================================================
    // USER ENDPOINTS (/api/users)
    // =========================================================================

    // Liet ke danh sach nguoi dung - chi Admin.
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> responses = userUseCase.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // Lay chi tiet nguoi dung - chi Admin.
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String userId) {
        UserResponse response = userUseCase.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Cap nhat ten hien thi (admin duoc phep sua bat ky user nao).
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        UserResponse response = userUseCase.updateUserProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Cap nhat nguoi dung thanh cong", response));
    }

    // Doi mat khau (admin doi ho user tu doi).
    @PatchMapping("/{userId}/password")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable String userId,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userUseCase.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Cap nhat mat khau nguoi dung thanh cong", null));
    }

    // Xoa nguoi dung (admin only).
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String userId) {
        userUseCase.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success("Da xoa nguoi dung thanh cong", null));
    }

    // =========================================================================
    // Phase 2 (P1): ADMIN-ONLY - Lock / Unlock tai khoan
    // =========================================================================
    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        UserResponse response = userUseCase.updateUserStatus(userId, request);
        return ResponseEntity.ok(ApiResponse.success(
                "Cap nhat trang thai nguoi dung thanh cong", response));
    }

    // =========================================================================
    // Phase 2 (P1): ADMIN-ONLY - Thay doi role / phan quyen
    // =========================================================================
    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<UserResponse>> changeUserRole(
            @PathVariable String userId,
            @Valid @RequestBody ChangeUserRoleRequest request
    ) {
        UserResponse response = userUseCase.changeUserRole(userId, request);
        return ResponseEntity.ok(ApiResponse.success(
                "Cap nhat vai tro nguoi dung thanh cong", response));
    }
}

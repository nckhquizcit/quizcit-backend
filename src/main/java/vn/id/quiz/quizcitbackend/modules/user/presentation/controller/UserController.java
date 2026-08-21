package vn.id.quiz.quizcitbackend.modules.user.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.ChangePasswordRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.AdminResetPasswordRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.ChangeUserRoleRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UpdateUserProfileRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UpdateUserStatusRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UserResponse;
import vn.id.quiz.quizcitbackend.modules.user.service.UserUseCase;
import vn.id.quiz.quizcitbackend.modules.user.service.UserEmailChangeService;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.VerifyEmailChangeOtpRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.EmailChangeChallengeResponse;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.VerifyOldEmailOtpRequest;
import vn.id.quiz.quizcitbackend.shared.security.CustomUserPrincipal;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import vn.id.quiz.quizcitbackend.shared.dto.PageResponse;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserUseCase userUseCase;
    private final UserEmailChangeService emailChangeService;

    // Lay ho so cua user dang dang nhap.
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        UserResponse response = userUseCase.getUserById(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Người dùng tự cập nhật tên hiển thị; email có luồng OTP riêng bên dưới.
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUserProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        UserResponse response = userUseCase.updateUserProfile(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ thành công", response));
    }

    // USER tu doi mat khau cua chinh minh.
    @PatchMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changeCurrentUserPassword(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userUseCase.changePassword(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật mật khẩu thành công", null));
    }

    @PostMapping("/me/email-change")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EmailChangeChallengeResponse>> requestEmailChange(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đã gửi mã xác thực đến email hiện tại",
                emailChangeService.requestOldEmailOtp(principal.getUserId())));
    }

    @PostMapping("/me/email-change/verify-old")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EmailChangeChallengeResponse>> verifyOldEmail(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody VerifyOldEmailOtpRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đã gửi mã xác thực đến email mới",
                emailChangeService.verifyOldEmailOtp(principal.getUserId(), request)));
    }

    @PostMapping("/me/email-change/confirm")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> confirmNewEmail(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody VerifyEmailChangeOtpRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Thay đổi email thành công",
                emailChangeService.confirmNewEmail(principal.getUserId(), request)));
    }

    // =========================================================================
    // USER ENDPOINTS (/api/users)
    // =========================================================================

    // Liet ke danh sach nguoi dung - chi Admin.
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Pageable bounded = PageRequest.of(pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), 100), pageable.getSort());
        Page<UserResponse> responses = userUseCase.getAllUsers(bounded);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(responses)));
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
        return ResponseEntity.ok(ApiResponse.success("Cập nhật người dùng thành công", response));
    }

    // Doi mat khau (admin doi ho user tu doi).
    @PatchMapping("/{userId}/password")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable String userId,
            @Valid @RequestBody AdminResetPasswordRequest request
    ) {
        userUseCase.resetPasswordByAdmin(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật mật khẩu người dùng thành công", null));
    }

    // Xoa nguoi dung (admin only).
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String userId,
            @RequestParam(name = "hard", defaultValue = "false") boolean hard) {
        if (hard) {
            userUseCase.hardDeleteUser(principal.getUserId(), userId);
        } else {
            userUseCase.deleteUser(principal.getUserId(), userId);
        }
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // Phase 2 (P1): ADMIN-ONLY - Lock / Unlock tai khoan
    // =========================================================================
    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        UserResponse response = userUseCase.updateUserStatus(principal.getUserId(), userId, request);
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật trạng thái người dùng thành công", response));
    }

    // =========================================================================
    // Phase 2 (P1): ADMIN-ONLY - Thay doi role / phan quyen
    // =========================================================================
    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<UserResponse>> changeUserRole(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String userId,
            @Valid @RequestBody ChangeUserRoleRequest request
    ) {
        UserResponse response = userUseCase.changeUserRole(principal.getUserId(), userId, request);
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật vai trò người dùng thành công", response));
    }

    // =========================================================================
    // Role & Permission Management (Admin Only)
    // =========================================================================

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<List<vn.id.quiz.quizcitbackend.modules.user.presentation.model.Role>>> getAllRoles() {
        return ResponseEntity.ok(ApiResponse.success(userUseCase.getAllRoles()));
    }

    @DeleteMapping("/roles/{roleId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(
            @PathVariable String roleId,
            @RequestParam(name = "hard", defaultValue = "false") boolean hard) {
        if (hard) {
            userUseCase.hardDeleteRole(roleId);
        } else {
            userUseCase.deleteRole(roleId);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<List<vn.id.quiz.quizcitbackend.modules.user.presentation.model.Permission>>> getAllPermissions() {
        return ResponseEntity.ok(ApiResponse.success(userUseCase.getAllPermissions()));
    }

    @DeleteMapping("/permissions/{permissionId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> deletePermission(
            @PathVariable String permissionId,
            @RequestParam(name = "hard", defaultValue = "false") boolean hard) {
        if (hard) {
            userUseCase.hardDeletePermission(permissionId);
        } else {
            userUseCase.deletePermission(permissionId);
        }
        return ResponseEntity.noContent().build();
    }
}

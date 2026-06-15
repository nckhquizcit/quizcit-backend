package vn.id.quiz.quizcitbackend.modules.user.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.ChangePasswordRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UpdateUserProfileRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UserResponse;
import vn.id.quiz.quizcitbackend.modules.user.service.UserUseCase;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserUseCase userUseCase;

    // =========================================================================
    // USER ENDPOINTS (/api/users)
    // =========================================================================

    // Flat Route: Lay danh sach tat ca nguoi dung
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> responses = userUseCase.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // Flat Route: Lay chi tiet 1 nguoi dung bang ID
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String userId) {
        UserResponse response = userUseCase.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Flat Route: Cap nhat ten hien thi cua nguoi dung
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> updateUserProfile(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        userUseCase.updateUserProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Cap nhat nguoi dung thanh cong", null));
    }

    // Flat Route: Doi mat khau nguoi dung
    @PatchMapping("/{userId}/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable String userId,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userUseCase.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Cap nhat mat khau nguoi dung thanh cong", null));
    }

    // Flat Route: Xoa nguoi dung va quan he vai tro lien quan
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String userId) {
        userUseCase.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success("Da xoa nguoi dung thanh cong", null));
    }
}

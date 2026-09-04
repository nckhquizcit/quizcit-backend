package vn.id.quiz.quizcitbackend.modules.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.quiz.quizcitbackend.modules.auth.service.PasswordService;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.ChangePasswordRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.AdminResetPasswordRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.ChangeUserRoleRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UpdateUserProfileRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UpdateUserStatusRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UserResponse;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.Role;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRoleAssignment;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserStatus;
import vn.id.quiz.quizcitbackend.modules.user.repository.UserRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserUseCase {
    private static final int PASSWORD_MIN_LENGTH = 6;
    private static final int PASSWORD_MAX_LENGTH = 255;
    private static final int DISPLAY_NAME_MAX_LENGTH = 100;

    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final PasswordService passwordService;

    // Doc danh sach users va gan role hien tai vao DTO cho frontend.
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        Map<String, UserRole> roles = userRepository.findRolesByUserIds(
                users.stream().map(User::getUserId).toList());
        return users.stream()
                .map(user -> mapper.toUserResponse(user, roles.get(user.getUserId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        Map<String, UserRole> roles = userRepository.findRolesByUserIds(
                users.getContent().stream().map(User::getUserId).toList());
        return users.map(user -> mapper.toUserResponse(user, roles.get(user.getUserId())));
    }

    // Lay chi tiet user; neu khong co thi nem AppException de GlobalExceptionHandler xu ly.
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(String userId) {
        User user = getRequiredUser(userId);
        return mapper.toUserResponse(user, resolveUserRole(userId));
    }

    // Chi cap nhat ten hien thi theo tai lieu kien truc.
    @Override
    @Transactional
    public UserResponse updateUserProfile(String userId, UpdateUserProfileRequest request) {
        request = requireRequest(request);
        String displayName = validateDisplayName(request.displayName());
        if (request.email() != null) {
            throw new AppException(ErrorMessages.VERIFIED_EMAIL_CHANGE_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        User user = getRequiredUser(userId);
        user.setDisplayName(displayName);
        if (request.avatarUrl() != null) {
            String trimmedAvatar = request.avatarUrl().trim();
            user.setAvatarUrl(trimmedAvatar.isEmpty() ? null : trimmedAvatar);
        }
        userRepository.save(user);
        return mapper.toUserResponse(user, resolveUserRole(userId));
    }

    // Doi mat khau va luu dang hash, cung co che voi module auth.
    @Override
    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request) {
        request = requireRequest(request);
        User user = getRequiredUser(userId);
        if (!passwordService.matches(request.currentPassword(), user.getPassword())) {
            throw new AppException(ErrorMessages.INVALID_CURRENT_PASSWORD, HttpStatus.UNAUTHORIZED);
        }
        String password = validatePassword(request.newPassword());
        user.setPassword(passwordService.hash(password));
        user.revokeSessions();
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPasswordByAdmin(String userId, AdminResetPasswordRequest request) {
        request = requireRequest(request);
        User user = getRequiredUser(userId);
        user.setPassword(passwordService.hash(validatePassword(request.newPassword())));
        user.revokeSessions();
        userRepository.save(user);
    }

    // Xóa mềm và ẩn danh user, giữ lại dữ liệu học tập liên quan.
    @Override
    @Transactional
    public void deleteUser(String actorUserId, String userId) {
        rejectSelfLockout(actorUserId, userId);
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(userId);
    }

    // Xóa cứng vĩnh viễn user và toàn bộ dữ liệu phụ thuộc.
    @Override
    @Transactional
    public void hardDeleteUser(String actorUserId, String userId) {
        rejectSelfLockout(actorUserId, userId);
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        userRepository.hardDeleteById(userId);
    }

    // =========================================================================
    // Phase 2 (P1): Admin khoa / mo khoa tai khoan.
    // =========================================================================
    @Override
    @Transactional
    public UserResponse updateUserStatus(String actorUserId, String userId, UpdateUserStatusRequest request) {
        rejectSelfLockout(actorUserId, userId);
        requireRequest(request);
        UserStatus target = request.getStatus();
        if (target == null) {
            throw new AppException(ErrorMessages.INVALID_ACCOUNT_STATUS, HttpStatus.BAD_REQUEST);
        }

        User user = getRequiredUser(userId);
        user.setStatus(target);
        user.revokeSessions();
        userRepository.save(user);

        return mapper.toUserResponse(user, resolveUserRole(userId));
    }

    // =========================================================================
    // Phase 2 (P1): Admin thay doi role cho user.
    // =========================================================================
    @Override
    @Transactional
    public UserResponse changeUserRole(String actorUserId, String userId, ChangeUserRoleRequest request) {
        rejectSelfLockout(actorUserId, userId);
        requireRequest(request);
        UserRole targetRole = request.getRole();
        if (targetRole == null) {
            throw new AppException(ErrorMessages.INVALID_ROLE, HttpStatus.BAD_REQUEST);
        }
        getRequiredUser(userId); // dam bao user ton tai

        Role role = userRepository.findRoleByName(targetRole)
                .orElseThrow(() -> new AppException(
                        ErrorMessages.ROLE_NOT_CONFIGURED,
                        HttpStatus.INTERNAL_SERVER_ERROR));

        // Xoa cac lien ket cu va gan moi
        userRepository.deleteRolesByUserId(userId);
        UserRoleAssignment assignment = UserRoleAssignment.assign(userId, role.getRoleId());
        userRepository.save(assignment);

        User user = getRequiredUser(userId);
        user.revokeSessions();
        userRepository.save(user);

        return getUserById(userId);
    }

    private void rejectSelfLockout(String actorUserId, String targetUserId) {
        if (actorUserId != null && actorUserId.equals(targetUserId)) {
            throw new AppException(
                    ErrorMessages.SELF_ADMIN_CHANGE_DENIED,
                    HttpStatus.BAD_REQUEST);
        }
    }

    // Dung chung cho cac nghiep vu can dam bao user ton tai truoc khi xu ly.
    private User getRequiredUser(String userId) {
        return userRepository.findUserById(userId)
                .orElseThrow(() -> new AppException(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private <T> T requireRequest(T request) {
        if (request == null) {
            throw new AppException(ErrorMessages.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        return request;
    }

    private String validatePassword(String password) {
        String value = requireNotBlank(password, ErrorMessages.PASSWORD_REQUIRED);
        if (value.length() < PASSWORD_MIN_LENGTH || value.length() > PASSWORD_MAX_LENGTH) {
            throw new AppException(ErrorMessages.PASSWORD_LENGTH, HttpStatus.BAD_REQUEST);
        }
        return value;
    }

    private String validateDisplayName(String displayName) {
        String normalized = requireNotBlank(displayName, ErrorMessages.DISPLAY_NAME_REQUIRED).trim();
        if (normalized.length() > DISPLAY_NAME_MAX_LENGTH) {
            throw new AppException(ErrorMessages.DISPLAY_NAME_LENGTH, HttpStatus.BAD_REQUEST);
        }
        return normalized;
    }

    private String requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new AppException(message, HttpStatus.BAD_REQUEST);
        }
        return value;
    }


    // Lay role hien tai cua user tu bang noi user_roles.
    private UserRole resolveUserRole(String userId) {
        return userRepository.findRoleByUserId(userId)
                .map(Role::getName)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return userRepository.findAllRoles();
    }

    @Override
    @Transactional
    public void deleteRole(String roleId) {
        userRepository.findRoleById(roleId)
                .orElseThrow(() -> new AppException("Không tìm thấy vai trò", HttpStatus.NOT_FOUND));
        userRepository.deleteRoleById(roleId);
    }

    @Override
    @Transactional
    public void hardDeleteRole(String roleId) {
        userRepository.findRoleById(roleId)
                .orElseThrow(() -> new AppException("Không tìm thấy vai trò", HttpStatus.NOT_FOUND));
        userRepository.hardDeleteRoleById(roleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<vn.id.quiz.quizcitbackend.modules.user.presentation.model.Permission> getAllPermissions() {
        return userRepository.findAllPermissions();
    }

    @Override
    @Transactional
    public void deletePermission(String permissionId) {
        userRepository.findPermissionById(permissionId)
                .orElseThrow(() -> new AppException("Không tìm thấy quyền", HttpStatus.NOT_FOUND));
        userRepository.deletePermissionById(permissionId);
    }

    @Override
    @Transactional
    public void hardDeletePermission(String permissionId) {
        userRepository.findPermissionById(permissionId)
                .orElseThrow(() -> new AppException("Không tìm thấy quyền", HttpStatus.NOT_FOUND));
        userRepository.hardDeletePermissionById(permissionId);
    }
}

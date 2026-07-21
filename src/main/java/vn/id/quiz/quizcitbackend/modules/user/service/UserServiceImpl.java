package vn.id.quiz.quizcitbackend.modules.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.quiz.quizcitbackend.modules.auth.service.PasswordService;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.ChangePasswordRequest;
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
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserUseCase {
    private static final int PASSWORD_MIN_LENGTH = 6;
    private static final int PASSWORD_MAX_LENGTH = 255;
    private static final int DISPLAY_NAME_MAX_LENGTH = 100;
    private static final int EMAIL_MAX_LENGTH = 100;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final PasswordService passwordService;

    // Doc danh sach users va gan role hien tai vao DTO cho frontend.
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> mapper.toUserResponse(user, resolveUserRole(user.getUserId())))
                .toList();
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
        String email = validateEmail(request.email(), userId);

        User user = getRequiredUser(userId);
        user.setDisplayName(displayName);
        if (email != null) {
            user.setEmail(email);
        }
        userRepository.save(user);
        return mapper.toUserResponse(user, resolveUserRole(userId));
    }

    // Doi mat khau va luu dang hash, cung co che voi module auth.
    @Override
    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request) {
        request = requireRequest(request);
        String password = validatePassword(request.password());

        User user = getRequiredUser(userId);
        user.setPassword(passwordService.hash(password));
        userRepository.save(user);
    }

    // Xoa user sau khi kiem tra ton tai; adapter se xoa user_roles lien quan truoc.
    @Override
    @Transactional
    public void deleteUser(String actorUserId, String userId) {
        rejectSelfLockout(actorUserId, userId);
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(userId);
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
            throw new AppException("Trạng thái tài khoản không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        User user = getRequiredUser(userId);
        user.setStatus(target);
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
                        "Vai trò không tồn tại trong hệ thống",
                        HttpStatus.INTERNAL_SERVER_ERROR));

        // Xoa cac lien ket cu va gan moi
        userRepository.deleteRolesByUserId(userId);
        UserRoleAssignment assignment = UserRoleAssignment.assign(userId, role.getRoleId());
        userRepository.save(assignment);

        return getUserById(userId);
    }

    private void rejectSelfLockout(String actorUserId, String targetUserId) {
        if (actorUserId != null && actorUserId.equals(targetUserId)) {
            throw new AppException(
                    "Bạn không thể khóa, xóa hoặc thay đổi vai trò tài khoản đang đăng nhập",
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
            throw new AppException("Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST);
        }
        return request;
    }

    private String validatePassword(String password) {
        String value = requireNotBlank(password, "Mật khẩu không được để trống");
        if (value.length() < PASSWORD_MIN_LENGTH || value.length() > PASSWORD_MAX_LENGTH) {
            throw new AppException("Mật khẩu phải từ 6 đến 255 ký tự", HttpStatus.BAD_REQUEST);
        }
        return value;
    }

    private String validateDisplayName(String displayName) {
        String normalized = requireNotBlank(displayName, "Tên hiển thị không được để trống").trim();
        if (normalized.length() > DISPLAY_NAME_MAX_LENGTH) {
            throw new AppException("Tên hiển thị không được vượt quá 100 ký tự", HttpStatus.BAD_REQUEST);
        }
        return normalized;
    }

    private String validateEmail(String email, String userId) {
        if (email == null) {
            return null;
        }

        String normalized = requireNotBlank(email, "Email không được để trống").trim();
        if (normalized.length() > EMAIL_MAX_LENGTH) {
            throw new AppException("Email không được vượt quá 100 ký tự", HttpStatus.BAD_REQUEST);
        }
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new AppException("Email không hợp lệ", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByEmailAndUserIdNot(normalized, userId)) {
            throw new AppException("Email đã được sử dụng", HttpStatus.BAD_REQUEST);
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
}

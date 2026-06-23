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

import java.time.ZonedDateTime;
import java.util.List;

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
    public void updateUserProfile(String userId, UpdateUserProfileRequest request) {
        request = requireRequest(request);
        String displayName = validateDisplayName(request.displayName());

        User user = getRequiredUser(userId);
        user.setDisplayName(displayName);
        userRepository.save(user);
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
    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException("Khong tim thay nguoi dung duoc yeu cau", HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(userId);
    }

    // =========================================================================
    // Phase 2 (P1): Admin khoa / mo khoa tai khoan.
    // =========================================================================
    @Override
    @Transactional
    public UserResponse updateUserStatus(String userId, UpdateUserStatusRequest request) {
        requireRequest(request);
        UserStatus target = request.getStatus();
        if (target == null) {
            throw new AppException("Trang thai tai khoan khong hop le", HttpStatus.BAD_REQUEST);
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
    public UserResponse changeUserRole(String userId, ChangeUserRoleRequest request) {
        requireRequest(request);
        UserRole targetRole = request.getRole();
        if (targetRole == null) {
            throw new AppException("Vai tro nguoi dung khong hop le", HttpStatus.BAD_REQUEST);
        }
        getRequiredUser(userId); // dam bao user ton tai

        Role role = userRepository.findRoleByName(targetRole)
                .orElseThrow(() -> new AppException(
                        "Vai tro khong ton tai trong he thong",
                        HttpStatus.INTERNAL_SERVER_ERROR));

        // Xoa cac lien ket cu va gan moi
        userRepository.deleteRolesByUserId(userId);
        UserRoleAssignment assignment = UserRoleAssignment.assign(userId, role.getRoleId());
        userRepository.save(assignment);

        return getUserById(userId);
    }

    // Dung chung cho cac nghiep vu can dam bao user ton tai truoc khi xu ly.
    private User getRequiredUser(String userId) {
        return userRepository.findUserById(userId)
                .orElseThrow(() -> new AppException("Khong tim thay nguoi dung duoc yeu cau", HttpStatus.NOT_FOUND));
    }

    private <T> T requireRequest(T request) {
        if (request == null) {
            throw new AppException("Yeu cau khong hop le", HttpStatus.BAD_REQUEST);
        }
        return request;
    }

    private String validatePassword(String password) {
        String value = requireNotBlank(password, "Mat khau khong duoc de trong");
        if (value.length() < PASSWORD_MIN_LENGTH || value.length() > PASSWORD_MAX_LENGTH) {
            throw new AppException("Mat khau phai tu 6 den 255 ky tu", HttpStatus.BAD_REQUEST);
        }
        return value;
    }

    private String validateDisplayName(String displayName) {
        String normalized = requireNotBlank(displayName, "Ten hien thi khong duoc de trong").trim();
        if (normalized.length() > DISPLAY_NAME_MAX_LENGTH) {
            throw new AppException("Ten hien thi khong duoc vuot qua 100 ky tu", HttpStatus.BAD_REQUEST);
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

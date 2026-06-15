package vn.id.quiz.quizcitbackend.modules.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.ChangePasswordRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.ChangeUserRoleRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.ChangeUserStatusRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.CreateUserRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UpdateUserProfileRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UserResponse;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.Role;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRoleAssignment;
import vn.id.quiz.quizcitbackend.modules.user.repository.UserRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserUseCase {
    private final UserRepository userRepository;
    private final UserMapper mapper;

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

    // Tao user trong bang users, sau do gan role mac dinh USER trong bang user_roles.
    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new AppException("Ten dang nhap da ton tai", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new AppException("Email da ton tai", HttpStatus.CONFLICT);
        }

        User user = User.create(request.username(), request.password(), request.email(), request.displayName());
        User savedUser = userRepository.save(user);

        Role defaultRole = getOrCreateRole(UserRole.USER);
        userRepository.save(UserRoleAssignment.assign(savedUser.getUserId(), defaultRole.getRoleId()));

        return mapper.toUserResponse(savedUser, defaultRole.getName());
    }

    // Chi cap nhat thong tin profile, khong doi role/status/password tai endpoint nay.
    @Override
    @Transactional
    public void updateUserProfile(String userId, UpdateUserProfileRequest request) {
        User user = getRequiredUser(userId);
        if (userRepository.existsByUsernameAndUserIdNot(request.username(), userId)) {
            throw new AppException("Ten dang nhap da ton tai", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByEmailAndUserIdNot(request.email(), userId)) {
            throw new AppException("Email da ton tai", HttpStatus.CONFLICT);
        }

        user.updateProfile(request.username(), request.email(), request.displayName());
        userRepository.save(user);
    }

    // RBAC duoc tach bang: doi role bang cach thay ban ghi trong user_roles.
    @Override
    @Transactional
    public void changeUserRole(String userId, ChangeUserRoleRequest request) {
        getRequiredUser(userId);
        Role role = getOrCreateRole(request.role());

        userRepository.deleteRolesByUserId(userId);
        userRepository.save(UserRoleAssignment.assign(userId, role.getRoleId()));
    }

    // Trang thai tai khoan van nam truc tiep trong bang users.
    @Override
    @Transactional
    public void changeUserStatus(String userId, ChangeUserStatusRequest request) {
        User user = getRequiredUser(userId);
        user.changeStatus(request.status());
        userRepository.save(user);
    }

    // Hien tai password duoc luu theo schema hien co; nen hash truoc khi dung production.
    @Override
    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request) {
        User user = getRequiredUser(userId);
        user.changePassword(request.password());
        userRepository.save(user);
    }

    // Shortcut nghiep vu: khoa tai khoan bang status BANNED.
    @Override
    @Transactional
    public void banUser(String userId) {
        User user = getRequiredUser(userId);
        user.ban();
        userRepository.save(user);
    }

    // Shortcut nghiep vu: xac minh tai khoan bang status VERIFIED.
    @Override
    @Transactional
    public void verifyUser(String userId) {
        User user = getRequiredUser(userId);
        user.verify();
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

    // Dung chung cho cac nghiep vu can dam bao user ton tai truoc khi xu ly.
    private User getRequiredUser(String userId) {
        return userRepository.findUserById(userId)
                .orElseThrow(() -> new AppException("Khong tim thay nguoi dung duoc yeu cau", HttpStatus.NOT_FOUND));
    }

    // Neu bang roles chua co role can dung thi tao moi theo enum UserRole.
    private Role getOrCreateRole(UserRole roleName) {
        if (roleName == null) {
            throw new AppException("Vai tro nguoi dung khong hop le", HttpStatus.BAD_REQUEST);
        }

        return userRepository.findRoleByName(roleName)
                .orElseGet(() -> userRepository.save(Role.create(roleName)));
    }

    // Lay role hien tai cua user tu bang noi user_roles.
    private UserRole resolveUserRole(String userId) {
        return userRepository.findRoleByUserId(userId)
                .map(Role::getName)
                .orElse(null);
    }
}

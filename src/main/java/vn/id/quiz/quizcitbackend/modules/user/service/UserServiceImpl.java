package vn.id.quiz.quizcitbackend.modules.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.ChangeUserRoleRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.ChangeUserStatusRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.CreateUserRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UpdateUserProfileRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UserResponse;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.repository.UserRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserUseCase {
    private final UserRepository userRepository;
    private final UserMapper mapper;

    @Override
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return mapper.toUserResponseList(users);
    }

    @Override
    public UserResponse getUserById(String userId) {
        User user = getRequiredUser(userId);
        return mapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new AppException("Tên đăng nhập đã tồn tại", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new AppException("Email đã tồn tại", HttpStatus.CONFLICT);
        }

        User user = User.create(request.username(), request.email(), request.displayName());
        return mapper.toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void updateUserProfile(String userId, UpdateUserProfileRequest request) {
        User user = getRequiredUser(userId);
        if (userRepository.existsByUsernameAndUserIdNot(request.username(), userId)) {
            throw new AppException("Tên đăng nhập đã tồn tại", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByEmailAndUserIdNot(request.email(), userId)) {
            throw new AppException("Email đã tồn tại", HttpStatus.CONFLICT);
        }

        user.updateProfile(request.username(), request.email(), request.displayName());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changeUserRole(String userId, ChangeUserRoleRequest request) {
        User user = getRequiredUser(userId);
        user.changeRole(request.role());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changeUserStatus(String userId, ChangeUserStatusRequest request) {
        User user = getRequiredUser(userId);
        user.changeStatus(request.status());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void banUser(String userId) {
        User user = getRequiredUser(userId);
        user.ban();
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void verifyUser(String userId) {
        User user = getRequiredUser(userId);
        user.verify();
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException("Không tìm thấy người dùng được yêu cầu", HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(userId);
    }

    private User getRequiredUser(String userId) {
        return userRepository.findUserById(userId)
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng được yêu cầu", HttpStatus.NOT_FOUND));
    }
}

package vn.id.quiz.quizcitbackend.modules.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import vn.id.quiz.quizcitbackend.modules.auth.service.PasswordService;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.ChangePasswordRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.ChangeUserRoleRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UpdateUserProfileRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UpdateUserStatusRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.Role;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRoleAssignment;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserStatus;
import vn.id.quiz.quizcitbackend.modules.user.repository.UserRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceImplTest {
    private FakeUserRepository userRepository;
    private PasswordService passwordService;
    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        userRepository = new FakeUserRepository();
        passwordService = new PasswordService();
        service = new UserServiceImpl(userRepository, new UserMapper() {}, passwordService);
    }

    @Test
    void updateUserProfileShouldOnlyChangeDisplayName() {
        User user = User.create("student01", "hashed-password", "student01@example.com", "Student One");
        userRepository.user = user;

        service.updateUserProfile(user.getUserId(), new UpdateUserProfileRequest("  Student Updated  "));

        assertThat(user.getUsername()).isEqualTo("student01");
        assertThat(user.getEmail()).isEqualTo("student01@example.com");
        assertThat(user.getDisplayName()).isEqualTo("Student Updated");
        assertThat(userRepository.savedUser).isSameAs(user);
    }

    @Test
    void updateUserProfileShouldUpdateAvatarUrlWhenProvided() {
        User user = User.create("student01", "hashed-password", "student01@example.com", "Student One");
        userRepository.user = user;

        service.updateUserProfile(user.getUserId(), new UpdateUserProfileRequest(
                "Student Updated",
                null,
                "/api/uploads/avatars/avatar-123.webp"
        ));

        assertThat(user.getDisplayName()).isEqualTo("Student Updated");
        assertThat(user.getAvatarUrl()).isEqualTo("/api/uploads/avatars/avatar-123.webp");
        assertThat(userRepository.savedUser).isSameAs(user);
    }

    @Test
    void updateUserProfileShouldClearAvatarUrlWhenEmptyStringProvided() {
        User user = User.create("student01", "hashed-password", "student01@example.com", "Student One");
        user.setAvatarUrl("/api/uploads/avatars/avatar-123.webp");
        userRepository.user = user;

        service.updateUserProfile(user.getUserId(), new UpdateUserProfileRequest(
                "Student Updated",
                null,
                "   "
        ));

        assertThat(user.getDisplayName()).isEqualTo("Student Updated");
        assertThat(user.getAvatarUrl()).isNull();
        assertThat(userRepository.savedUser).isSameAs(user);
    }

    @Test
    void updateUserProfileShouldRequireVerifiedFlowWhenEmailProvided() {
        User user = User.create("student01", "hashed-password", "student01@example.com", "Student One");
        userRepository.user = user;

        assertThatThrownBy(() -> service.updateUserProfile(
                user.getUserId(),
                new UpdateUserProfileRequest("Student Updated", "student.updated@example.com")
        )).isInstanceOf(AppException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST.value());

        assertThat(user.getEmail()).isEqualTo("student01@example.com");
        assertThat(userRepository.savedUser).isNull();
    }

    @Test
    void updateUserProfileShouldRejectDuplicateEmailBeforeSaving() {
        User user = User.create("student01", "hashed-password", "student01@example.com", "Student One");
        userRepository.user = user;
        userRepository.emailExistsForOtherUser = true;

        assertThatThrownBy(() -> service.updateUserProfile(
                user.getUserId(),
                new UpdateUserProfileRequest("Student Updated", "used@example.com")
        ))
                .isInstanceOf(AppException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST.value());

        assertThat(userRepository.savedUser).isNull();
    }

    @Test
    void updateUserProfileShouldRejectBlankDisplayNameBeforeSaving() {
        assertThatThrownBy(() -> service.updateUserProfile("user-id", new UpdateUserProfileRequest(" ")))
                .isInstanceOf(AppException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST.value());

        assertThat(userRepository.savedUser).isNull();
    }

    @Test
    void changePasswordShouldHashAndPersistNewPassword() {
        User user = User.create("student01", passwordService.hash("oldSecret123"), "student01@example.com", "Student One");
        userRepository.user = user;

        service.changePassword(user.getUserId(), new ChangePasswordRequest("oldSecret123", "newSecret123"));

        assertThat(user.getPassword()).startsWith("pbkdf2$");
        assertThat(user.getPassword()).isNotEqualTo("newSecret123");
        assertThat(passwordService.matches("newSecret123", user.getPassword())).isTrue();
        assertThat(userRepository.savedUser).isSameAs(user);
    }

    @Test
    void changePasswordShouldRejectBlankPasswordBeforeHashing() {
        User user = User.create("student01", passwordService.hash("oldSecret123"), "student01@example.com", "Student One");
        userRepository.user = user;
        assertThatThrownBy(() -> service.changePassword("user-id", new ChangePasswordRequest("oldSecret123", " ")))
                .isInstanceOf(AppException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST.value());

        assertThat(userRepository.savedUser).isNull();
    }

    @Test
    void changePasswordShouldRejectWrongCurrentPassword() {
        User user = User.create("student01", passwordService.hash("oldSecret123"),
                "student01@example.com", "Student One");
        userRepository.user = user;

        assertThatThrownBy(() -> service.changePassword(user.getUserId(),
                new ChangePasswordRequest("wrongSecret", "newSecret123")))
                .isInstanceOf(AppException.class)
                .extracting("status").isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(passwordService.matches("oldSecret123", user.getPassword())).isTrue();
        assertThat(userRepository.savedUser).isNull();
    }

    @Test
    void deleteUserShouldDeleteExistingUser() {
        userRepository.existsById = true;

        service.deleteUser("admin-id", "user-id");

        assertThat(userRepository.deletedUserId).isEqualTo("user-id");
    }

    @Test
    void deleteUserShouldRejectMissingUser() {
        userRepository.existsById = false;

        assertThatThrownBy(() -> service.deleteUser("admin-id", "missing-user"))
                .isInstanceOf(AppException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.NOT_FOUND.value());

        assertThat(userRepository.deletedUserId).isNull();
    }

    @Test
    void deleteUserShouldRejectSelfDelete() {
        assertThatThrownBy(() -> service.deleteUser("admin-id", "admin-id"))
                .isInstanceOf(AppException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST.value());

        assertThat(userRepository.deletedUserId).isNull();
    }

    @Test
    void updateUserStatusShouldRejectSelfStatusChange() {
        UpdateUserStatusRequest request = UpdateUserStatusRequest.builder()
                .status(UserStatus.BANNED)
                .build();

        assertThatThrownBy(() -> service.updateUserStatus("admin-id", "admin-id", request))
                .isInstanceOf(AppException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST.value());

        assertThat(userRepository.savedUser).isNull();
    }

    @Test
    void changeUserRoleShouldRejectSelfRoleChange() {
        ChangeUserRoleRequest request = ChangeUserRoleRequest.builder()
                .role(UserRole.USER)
                .build();

        assertThatThrownBy(() -> service.changeUserRole("admin-id", "admin-id", request))
                .isInstanceOf(AppException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private static class FakeUserRepository implements UserRepository {
        private User user;
        private User savedUser;
        private boolean existsById;
        private boolean emailExistsForOtherUser;
        private String deletedUserId;

        @Override
        public List<User> findAll() {
            return user == null ? List.of() : List.of(user);
        }

        @Override
        public User save(User user) {
            this.savedUser = user;
            return user;
        }

        @Override
        public Role save(Role role) {
            return role;
        }

        @Override
        public UserRoleAssignment save(UserRoleAssignment userRoleAssignment) {
            return userRoleAssignment;
        }

        @Override
        public Optional<User> findUserById(String userId) {
            return Optional.ofNullable(user);
        }

        @Override
        public Optional<Role> findRoleByName(UserRole name) {
            return Optional.empty();
        }

        @Override
        public Optional<Role> findRoleByUserId(String userId) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(String userId) {
            return existsById;
        }

        @Override
        public boolean existsByUsername(String username) {
            return false;
        }

        @Override
        public boolean existsByEmail(String email) {
            return false;
        }

        @Override
        public boolean existsByUsernameAndUserIdNot(String username, String userId) {
            return false;
        }

        @Override
        public boolean existsByEmailAndUserIdNot(String email, String userId) {
            return emailExistsForOtherUser;
        }

        @Override
        public void deleteRolesByUserId(String userId) {
        }

        @Override
        public void deleteById(String userId) {
            this.deletedUserId = userId;
        }
    }
}

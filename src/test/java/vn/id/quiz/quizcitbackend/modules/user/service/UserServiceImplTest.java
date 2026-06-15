package vn.id.quiz.quizcitbackend.modules.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.ChangePasswordRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.ChangeUserRoleRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.CreateUserRequest;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UserResponse;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.Role;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRoleAssignment;
import vn.id.quiz.quizcitbackend.modules.user.repository.UserRepository;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper mapper;

    @InjectMocks
    private UserServiceImpl service;

    @Test
    void createUserShouldSavePasswordAndAssignDefaultUserRole() {
        Role defaultRole = Role.create(UserRole.USER);
        CreateUserRequest request = new CreateUserRequest(
                "student01",
                "secret123",
                "student01@example.com",
                "Student One"
        );

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.findRoleByName(UserRole.USER)).thenReturn(Optional.of(defaultRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(UserRoleAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toUserResponse(any(User.class), eq(UserRole.USER))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return new UserResponse(
                    user.getUserId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getDisplayName(),
                    UserRole.USER,
                    user.getStatus(),
                    user.getCreatedAt(),
                    user.getUpdatedAt()
            );
        });

        UserResponse response = service.createUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("secret123");

        ArgumentCaptor<UserRoleAssignment> assignmentCaptor = ArgumentCaptor.forClass(UserRoleAssignment.class);
        verify(userRepository).save(assignmentCaptor.capture());
        assertThat(assignmentCaptor.getValue().getUserId()).isEqualTo(response.userId());
        assertThat(assignmentCaptor.getValue().getRoleId()).isEqualTo(defaultRole.getRoleId());
    }

    @Test
    void changeUserRoleShouldReplaceUserRoleAssignment() {
        User user = User.create("student01", "secret123", "student01@example.com", "Student One");
        Role adminRole = Role.create(UserRole.ADMINISTRATOR);

        when(userRepository.findUserById(user.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.findRoleByName(UserRole.ADMINISTRATOR)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(UserRoleAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.changeUserRole(user.getUserId(), new ChangeUserRoleRequest(UserRole.ADMINISTRATOR));

        verify(userRepository).deleteRolesByUserId(user.getUserId());
        ArgumentCaptor<UserRoleAssignment> assignmentCaptor = ArgumentCaptor.forClass(UserRoleAssignment.class);
        verify(userRepository).save(assignmentCaptor.capture());
        assertThat(assignmentCaptor.getValue().getUserId()).isEqualTo(user.getUserId());
        assertThat(assignmentCaptor.getValue().getRoleId()).isEqualTo(adminRole.getRoleId());
    }

    @Test
    void changePasswordShouldPersistNewPassword() {
        User user = User.create("student01", "secret123", "student01@example.com", "Student One");

        when(userRepository.findUserById(user.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.changePassword(user.getUserId(), new ChangePasswordRequest("newSecret123"));

        assertThat(user.getPassword()).isEqualTo("newSecret123");
        verify(userRepository).save(user);
    }
}

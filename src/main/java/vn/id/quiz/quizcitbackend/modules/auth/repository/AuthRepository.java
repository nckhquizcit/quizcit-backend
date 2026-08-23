package vn.id.quiz.quizcitbackend.modules.auth.repository;

import vn.id.quiz.quizcitbackend.modules.user.presentation.model.Role;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRoleAssignment;

import java.util.Optional;

public interface AuthRepository {
    User save(User user);

    Role save(Role role);

    UserRoleAssignment save(UserRoleAssignment userRoleAssignment);

    Optional<User> findUserByUsername(String username);

    Optional<User> findUserByEmail(String email);
    Optional<User> findUserById(String userId);

    default Optional<vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.AuthUserState> findUserAuthState(String userId) {
        return findUserById(userId)
                .map(user -> new vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.AuthUserState(
                        user.getUserId(), user.getStatus(), user.getTokenVersion()));
    }

    Optional<Role> findRoleByName(UserRole name);

    Optional<Role> findRoleByUserId(String userId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}

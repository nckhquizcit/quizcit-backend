package vn.id.quiz.quizcitbackend.modules.user.repository;

import vn.id.quiz.quizcitbackend.modules.user.presentation.model.Role;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRoleAssignment;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> findAll();

    User save(User user);

    Role save(Role role);

    UserRoleAssignment save(UserRoleAssignment userRoleAssignment);

    Optional<User> findUserById(String userId);

    Optional<Role> findRoleByName(UserRole name);

    Optional<Role> findRoleByUserId(String userId);

    boolean existsById(String userId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameAndUserIdNot(String username, String userId);

    boolean existsByEmailAndUserIdNot(String email, String userId);

    void deleteRolesByUserId(String userId);

    void deleteById(String userId);
}

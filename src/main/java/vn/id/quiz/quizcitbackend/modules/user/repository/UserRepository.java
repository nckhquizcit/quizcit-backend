package vn.id.quiz.quizcitbackend.modules.user.repository;

import vn.id.quiz.quizcitbackend.modules.user.presentation.model.Role;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRoleAssignment;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.id.quiz.quizcitbackend.modules.user.presentation.model.Permission;

public interface UserRepository {
    List<User> findAll();
    default Page<User> findAll(Pageable pageable) {
        List<User> users = findAll();
        int start = Math.min((int) pageable.getOffset(), users.size());
        int end = Math.min(start + pageable.getPageSize(), users.size());
        return new org.springframework.data.domain.PageImpl<>(users.subList(start, end), pageable, users.size());
    }

    User save(User user);

    Role save(Role role);

    UserRoleAssignment save(UserRoleAssignment userRoleAssignment);

    Optional<User> findUserById(String userId);

    Optional<Role> findRoleByName(UserRole name);

    default Optional<Role> findRoleById(String roleId) { return Optional.empty(); }

    default List<Role> findAllRoles() { return List.of(); }

    Optional<Role> findRoleByUserId(String userId);
    default Map<String, UserRole> findRolesByUserIds(Collection<String> userIds) {
        if (userIds == null) return Map.of();
        Map<String, UserRole> roles = new java.util.HashMap<>();
        userIds.forEach(id -> findRoleByUserId(id).map(Role::getName)
                .ifPresent(role -> roles.put(id, role)));
        return roles;
    }

    boolean existsById(String userId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameAndUserIdNot(String username, String userId);

    boolean existsByEmailAndUserIdNot(String email, String userId);

    void deleteRolesByUserId(String userId);

    void deleteById(String userId);

    default void hardDeleteById(String userId) {
        deleteById(userId);
    }

    default void deleteRoleById(String roleId) {}

    default void hardDeleteRoleById(String roleId) {}

    default Optional<Permission> findPermissionById(String permissionId) { return Optional.empty(); }

    default List<Permission> findAllPermissions() { return List.of(); }

    default Permission savePermission(Permission permission) { return permission; }

    default void deletePermissionById(String permissionId) {}

    default void hardDeletePermissionById(String permissionId) {}
}

package vn.id.quiz.quizcitbackend.modules.user.repository.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.Role;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRoleAssignment;
import vn.id.quiz.quizcitbackend.modules.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {
    // Adapter gom cac JpaRepository cua module user de service khong phu thuoc truc tiep JPA.
    private final JpaUserRepository jpaUserRepository;
    private final JpaRoleRepository jpaRoleRepository;
    private final JpaUserRoleRepository jpaUserRoleRepository;

    @Override
    public List<User> findAll() {
        return jpaUserRepository.findAll();
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return jpaUserRepository.findAll(pageable);
    }

    @Override
    public User save(User user) {
        return jpaUserRepository.save(user);
    }

    @Override
    public Role save(Role role) {
        return jpaRoleRepository.save(role);
    }

    @Override
    public UserRoleAssignment save(UserRoleAssignment userRoleAssignment) {
        return jpaUserRoleRepository.save(userRoleAssignment);
    }

    @Override
    public Optional<User> findUserById(String userId) {
        return jpaUserRepository.findById(userId);
    }

    @Override
    public Optional<Role> findRoleByName(UserRole name) {
        return jpaRoleRepository.findByName(name);
    }

    @Override
    public Optional<Role> findRoleByUserId(String userId) {
        // user_roles la bang noi nen can lay role_id truoc roi moi doc bang roles.
        return jpaUserRoleRepository.findByUserId(userId)
                .stream()
                .findFirst()
                .flatMap(userRole -> jpaRoleRepository.findById(userRole.getRoleId()));
    }

    @Override
    public Map<String, UserRole> findRolesByUserIds(Collection<String> userIds) {
        if (userIds == null || userIds.isEmpty()) return Map.of();
        List<UserRoleAssignment> assignments = jpaUserRoleRepository.findByUserIdIn(userIds);
        Map<String, Role> rolesById = jpaRoleRepository.findAllById(assignments.stream()
                .map(UserRoleAssignment::getRoleId).distinct().toList()).stream()
                .collect(Collectors.toMap(Role::getRoleId, role -> role));
        return assignments.stream().filter(assignment -> rolesById.containsKey(assignment.getRoleId()))
                .collect(Collectors.toMap(UserRoleAssignment::getUserId,
                        assignment -> rolesById.get(assignment.getRoleId()).getName(),
                        (first, ignored) -> first));
    }

    @Override
    public boolean existsById(String userId) {
        return jpaUserRepository.existsById(userId);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaUserRepository.existsByUsernameIgnoreCase(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public boolean existsByUsernameAndUserIdNot(String username, String userId) {
        return jpaUserRepository.existsByUsernameIgnoreCaseAndUserIdNot(username, userId);
    }

    @Override
    public boolean existsByEmailAndUserIdNot(String email, String userId) {
        return jpaUserRepository.existsByEmailIgnoreCaseAndUserIdNot(email, userId);
    }

    @Override
    public void deleteRolesByUserId(String userId) {
        jpaUserRoleRepository.deleteByUserId(userId);
    }

    @Override
    public void deleteById(String userId) {
        User user = jpaUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
        user.softDelete();
        jpaUserRepository.save(user);
    }
}

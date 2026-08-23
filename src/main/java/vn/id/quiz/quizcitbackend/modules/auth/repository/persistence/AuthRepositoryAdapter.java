package vn.id.quiz.quizcitbackend.modules.auth.repository.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.id.quiz.quizcitbackend.modules.auth.repository.AuthRepository;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.Role;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRoleAssignment;

import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.AuthUserState;
import vn.id.quiz.quizcitbackend.shared.config.CacheConfig;

@Component
@RequiredArgsConstructor
public class AuthRepositoryAdapter implements AuthRepository {
    private final JpaAuthUserRepository jpaAuthUserRepository;
    private final JpaAuthRoleRepository jpaAuthRoleRepository;
    private final JpaAuthUserRoleRepository jpaAuthUserRoleRepository;

    @Override
    @CacheEvict(value = CacheConfig.CACHE_AUTH_USER_STATE, key = "#user.userId")
    public User save(User user) {
        return jpaAuthUserRepository.save(user);
    }

    @Override
    public Role save(Role role) {
        return jpaAuthRoleRepository.save(role);
    }

    @Override
    public UserRoleAssignment save(UserRoleAssignment userRoleAssignment) {
        return jpaAuthUserRoleRepository.save(userRoleAssignment);
    }

    @Override
    public Optional<User> findUserByUsername(String username) {
        return jpaAuthUserRepository.findByUsernameIgnoreCase(username);
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return jpaAuthUserRepository.findByEmailIgnoreCase(email);
    }

    @Override
    public Optional<User> findUserById(String userId) {
        return jpaAuthUserRepository.findById(userId);
    }

    @Override
    @Cacheable(value = CacheConfig.CACHE_AUTH_USER_STATE, key = "#userId")
    public Optional<AuthUserState> findUserAuthState(String userId) {
        return jpaAuthUserRepository.findById(userId)
                .map(user -> new AuthUserState(user.getUserId(), user.getStatus(), user.getTokenVersion()));
    }

    @Override
    public Optional<Role> findRoleByName(UserRole name) {
        return jpaAuthRoleRepository.findByName(name);
    }

    @Override
    public Optional<Role> findRoleByUserId(String userId) {
        // user_roles la bang noi nen can lay role_id truoc roi moi doc bang roles.
        return jpaAuthUserRoleRepository.findByUserId(userId)
                .stream()
                .findFirst()
                .flatMap(userRole -> jpaAuthRoleRepository.findById(userRole.getRoleId()));
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaAuthUserRepository.existsByUsernameIgnoreCase(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaAuthUserRepository.existsByEmailIgnoreCase(email);
    }
}

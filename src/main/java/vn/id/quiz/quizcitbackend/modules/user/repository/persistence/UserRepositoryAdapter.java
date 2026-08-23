package vn.id.quiz.quizcitbackend.modules.user.repository.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.id.quiz.quizcitbackend.modules.auth.repository.persistence.JpaAuthChallengeRepository;
import vn.id.quiz.quizcitbackend.modules.auth.repository.persistence.JpaRefreshTokenRepository;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaUserCourseEnrollmentRepository;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaUserUnlockedChapterRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.persistence.JpaAbilityHistoryRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.persistence.JpaUserSkillAbilityRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Attempt;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence.JpaAdaptiveQuestionSelectionRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence.JpaAttemptRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence.JpaResponseRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence.JpaUserAdaptiveQuizTrackingRepository;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.Permission;
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

import org.springframework.cache.annotation.CacheEvict;
import vn.id.quiz.quizcitbackend.shared.config.CacheConfig;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {
    private final JpaUserRepository jpaUserRepository;
    private final JpaRoleRepository jpaRoleRepository;
    private final JpaUserRoleRepository jpaUserRoleRepository;
    private final JpaPermissionRepository jpaPermissionRepository;
    private final JpaRolePermissionRepository jpaRolePermissionRepository;
    private final JpaUserCourseEnrollmentRepository jpaUserCourseEnrollmentRepository;
    private final JpaUserUnlockedChapterRepository jpaUserUnlockedChapterRepository;
    private final JpaRefreshTokenRepository jpaRefreshTokenRepository;
    private final JpaAuthChallengeRepository jpaAuthChallengeRepository;
    private final JpaUserSkillAbilityRepository jpaUserSkillAbilityRepository;
    private final JpaAbilityHistoryRepository jpaAbilityHistoryRepository;
    private final JpaUserAchievementRepository jpaUserAchievementRepository;
    private final JpaUserAdaptiveQuizTrackingRepository jpaUserAdaptiveQuizTrackingRepository;
    private final JpaAdaptiveQuestionSelectionRepository jpaAdaptiveQuestionSelectionRepository;
    private final JpaAttemptRepository jpaAttemptRepository;
    private final JpaResponseRepository jpaResponseRepository;

    @Override
    public List<User> findAll() {
        return jpaUserRepository.findAll();
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return jpaUserRepository.findAll(pageable);
    }

    @Override
    @CacheEvict(value = CacheConfig.CACHE_AUTH_USER_STATE, key = "#user.userId")
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
    public Optional<Role> findRoleById(String roleId) {
        return jpaRoleRepository.findById(roleId);
    }

    @Override
    public List<Role> findAllRoles() {
        return jpaRoleRepository.findAll();
    }

    @Override
    public Optional<Role> findRoleByUserId(String userId) {
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
    @CacheEvict(value = CacheConfig.CACHE_AUTH_USER_STATE, key = "#userId")
    public void deleteById(String userId) {
        User user = jpaUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
        user.softDelete();
        jpaUserRepository.save(user);
    }

    @Override
    @CacheEvict(value = CacheConfig.CACHE_AUTH_USER_STATE, key = "#userId")
    public void hardDeleteById(String userId) {
        jpaUserRoleRepository.deleteByUserId(userId);
        jpaUserCourseEnrollmentRepository.deleteByUserId(userId);
        jpaUserUnlockedChapterRepository.deleteByUserId(userId);
        jpaRefreshTokenRepository.deleteByUserId(userId);
        jpaAuthChallengeRepository.deleteByUserId(userId);
        jpaUserSkillAbilityRepository.deleteByUserId(userId);
        jpaAbilityHistoryRepository.deleteByUserId(userId);
        jpaUserAchievementRepository.deleteByUserId(userId);
        jpaUserAdaptiveQuizTrackingRepository.deleteByUserId(userId);
        jpaAdaptiveQuestionSelectionRepository.deleteByUserId(userId);

        List<Attempt> attempts = jpaAttemptRepository.findByUserId(userId);
        if (!attempts.isEmpty()) {
            List<String> attemptIds = attempts.stream().map(Attempt::getAttemptId).toList();
            jpaResponseRepository.deleteByAttemptIdIn(attemptIds);
            jpaAttemptRepository.deleteByUserId(userId);
        }

        jpaUserRepository.deleteById(userId);
    }

    @Override
    public void deleteRoleById(String roleId) {
        jpaRoleRepository.findById(roleId).ifPresent(role -> {
            role.softDelete();
            jpaRoleRepository.save(role);
        });
    }

    @Override
    public void hardDeleteRoleById(String roleId) {
        jpaRolePermissionRepository.deleteByRoleId(roleId);
        jpaUserRoleRepository.deleteByRoleId(roleId);
        jpaRoleRepository.deleteById(roleId);
    }

    @Override
    public Optional<Permission> findPermissionById(String permissionId) {
        return jpaPermissionRepository.findById(permissionId);
    }

    @Override
    public List<Permission> findAllPermissions() {
        return jpaPermissionRepository.findAll();
    }

    @Override
    public Permission savePermission(Permission permission) {
        return jpaPermissionRepository.save(permission);
    }

    @Override
    public void deletePermissionById(String permissionId) {
        jpaPermissionRepository.findById(permissionId).ifPresent(permission -> {
            permission.softDelete();
            jpaPermissionRepository.save(permission);
        });
    }

    @Override
    public void hardDeletePermissionById(String permissionId) {
        jpaRolePermissionRepository.deleteByPermissionId(permissionId);
        jpaPermissionRepository.deleteById(permissionId);
    }
}

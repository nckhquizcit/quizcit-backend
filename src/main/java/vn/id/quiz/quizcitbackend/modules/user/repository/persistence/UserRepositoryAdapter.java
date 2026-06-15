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
    public boolean existsById(String userId) {
        return jpaUserRepository.existsById(userId);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaUserRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsernameAndUserIdNot(String username, String userId) {
        return jpaUserRepository.existsByUsernameAndUserIdNot(username, userId);
    }

    @Override
    public boolean existsByEmailAndUserIdNot(String email, String userId) {
        return jpaUserRepository.existsByEmailAndUserIdNot(email, userId);
    }

    @Override
    public void deleteRolesByUserId(String userId) {
        jpaUserRoleRepository.deleteByUserId(userId);
    }

    @Override
    public void deleteById(String userId) {
        // Xoa quan he role truoc de tranh loi khoa ngoai khi xoa user.
        jpaUserRoleRepository.deleteByUserId(userId);
        jpaUserRepository.deleteById(userId);
    }
}

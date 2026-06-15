package vn.id.quiz.quizcitbackend.modules.user.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.Role;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;

import java.util.Optional;

public interface JpaRoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByName(UserRole name);
}

package vn.id.quiz.quizcitbackend.modules.user.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.Permission;

import java.util.Optional;

public interface JpaPermissionRepository extends JpaRepository<Permission, String> {
    Optional<Permission> findByName(String name);
}

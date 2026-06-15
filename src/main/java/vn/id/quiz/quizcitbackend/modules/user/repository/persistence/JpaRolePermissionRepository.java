package vn.id.quiz.quizcitbackend.modules.user.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.RolePermissionAssignment;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.RolePermissionAssignmentId;

import java.util.List;

public interface JpaRolePermissionRepository extends JpaRepository<RolePermissionAssignment, RolePermissionAssignmentId> {
    List<RolePermissionAssignment> findByRoleId(String roleId);

    void deleteByRoleId(String roleId);
}

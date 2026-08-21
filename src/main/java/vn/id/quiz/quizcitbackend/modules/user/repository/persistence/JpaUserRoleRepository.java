package vn.id.quiz.quizcitbackend.modules.user.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRoleAssignment;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRoleAssignmentId;

import java.util.List;
import java.util.Collection;

public interface JpaUserRoleRepository extends JpaRepository<UserRoleAssignment, UserRoleAssignmentId> {
    List<UserRoleAssignment> findByUserId(String userId);
    List<UserRoleAssignment> findByUserIdIn(Collection<String> userIds);

    void deleteByUserId(String userId);
    void deleteByRoleId(String roleId);
}

package vn.id.quiz.quizcitbackend.modules.auth.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRoleAssignment;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRoleAssignmentId;

import java.util.List;

public interface JpaAuthUserRoleRepository extends JpaRepository<UserRoleAssignment, UserRoleAssignmentId> {
    List<UserRoleAssignment> findByUserId(String userId);
}

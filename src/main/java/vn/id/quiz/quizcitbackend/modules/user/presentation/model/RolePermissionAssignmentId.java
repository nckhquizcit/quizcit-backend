package vn.id.quiz.quizcitbackend.modules.user.presentation.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RolePermissionAssignmentId implements Serializable {
    private String roleId;
    private String permissionId;
}

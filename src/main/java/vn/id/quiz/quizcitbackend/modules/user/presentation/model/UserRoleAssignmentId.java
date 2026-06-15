package vn.id.quiz.quizcitbackend.modules.user.presentation.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserRoleAssignmentId implements Serializable {
    private String userId;
    private String roleId;
}

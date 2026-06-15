package vn.id.quiz.quizcitbackend.modules.user.presentation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

@Entity
@Table(name = "role_permissions")
@IdClass(RolePermissionAssignmentId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RolePermissionAssignment {
    @Id
    @Column(name = "role_id", length = 50)
    private String roleId;

    @Id
    @Column(name = "permission_id", length = 50)
    private String permissionId;

    public static RolePermissionAssignment assign(String roleId, String permissionId) {
        if (roleId == null || roleId.isBlank()) {
            throw new AppException("Vai trò người dùng không hợp lệ", HttpStatus.BAD_REQUEST);
        }
        if (permissionId == null || permissionId.isBlank()) {
            throw new AppException("Quyền không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        return RolePermissionAssignment.builder()
                .roleId(roleId)
                .permissionId(permissionId)
                .build();
    }
}

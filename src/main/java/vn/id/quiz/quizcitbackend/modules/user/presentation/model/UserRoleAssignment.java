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
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

@Entity
@Table(name = "user_roles")
@IdClass(UserRoleAssignmentId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserRoleAssignment {
    // Bang noi user_roles dung khoa chinh ghep user_id + role_id.
    @Id
    @Column(name = "user_id", length = 50)
    private String userId;

    @Id
    @Column(name = "role_id", length = 50)
    private String roleId;

    // Tao ban ghi gan vai tro cho user trong bang user_roles.
    public static UserRoleAssignment assign(String userId, String roleId) {
        if (userId == null || userId.isBlank()) {
            throw new AppException("Người dùng không hợp lệ", HttpStatus.BAD_REQUEST);
        }
        if (roleId == null || roleId.isBlank()) {
            throw new AppException(ErrorMessages.INVALID_ROLE, HttpStatus.BAD_REQUEST);
        }

        return UserRoleAssignment.builder()
                .userId(userId)
                .roleId(roleId)
                .build();
    }
}

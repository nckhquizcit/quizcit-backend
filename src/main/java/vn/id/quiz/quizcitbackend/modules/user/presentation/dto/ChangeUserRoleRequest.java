package vn.id.quiz.quizcitbackend.modules.user.presentation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;

/**
 * Phase 2 (P1) - Dùng cho admin đổi role của một user.
 *
 * <p>Chỉ chấp nhận {@code USER} hoặc {@code ADMINISTRATOR}.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeUserRoleRequest {

    @NotNull(message = "Vai trò người dùng không được để trống")
    private UserRole role;
}

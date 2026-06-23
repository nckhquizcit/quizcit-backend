package vn.id.quiz.quizcitbackend.modules.user.presentation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserStatus;

/**
 * Phase 2 (P1) - Dùng cho admin khoá / mở khoá tài khoản.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusRequest {

    @NotNull(message = "Trạng thái tài khoản không được để trống")
    private UserStatus status;
}

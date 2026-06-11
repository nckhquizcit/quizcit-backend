package vn.id.quiz.quizcitbackend.modules.user.presentation.dto;

import jakarta.validation.constraints.NotNull;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserStatus;

public record ChangeUserStatusRequest(
        @NotNull(message = "Trạng thái người dùng không được để trống")
        UserStatus status
) {
}

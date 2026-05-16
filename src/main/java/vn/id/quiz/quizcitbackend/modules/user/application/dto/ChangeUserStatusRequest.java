package vn.id.quiz.quizcitbackend.modules.user.application.dto;

import jakarta.validation.constraints.NotNull;
import vn.id.quiz.quizcitbackend.modules.user.domain.entity.UserStatus;

public record ChangeUserStatusRequest(
        @NotNull(message = "Trạng thái người dùng không được để trống")
        UserStatus status
) {
}

package vn.id.quiz.quizcitbackend.modules.user.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

public record UpdateUserProfileRequest(
        @NotBlank(message = ErrorMessages.DISPLAY_NAME_REQUIRED)
        @Size(max = 100, message = ErrorMessages.DISPLAY_NAME_LENGTH)
        String displayName,

        @Email(message = ErrorMessages.EMAIL_INVALID)
        @Size(max = 100, message = ErrorMessages.EMAIL_LENGTH)
        String email,

        @Size(max = 500, message = "Đường dẫn ảnh đại diện không hợp lệ")
        String avatarUrl
) {
    public UpdateUserProfileRequest(String displayName) {
        this(displayName, null, null);
    }

    public UpdateUserProfileRequest(String displayName, String email) {
        this(displayName, email, null);
    }
}

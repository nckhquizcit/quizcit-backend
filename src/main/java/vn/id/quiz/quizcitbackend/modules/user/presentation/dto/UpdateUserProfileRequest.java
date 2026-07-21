package vn.id.quiz.quizcitbackend.modules.user.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @NotBlank(message = "Tên hiển thị không được để trống")
        @Size(max = 100, message = "Tên hiển thị không được vượt quá 100 ký tự")
        String displayName,

        @Email(message = "Email không hợp lệ")
        @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
        String email
) {
    public UpdateUserProfileRequest(String displayName) {
        this(displayName, null);
    }
}

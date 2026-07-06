package vn.id.quiz.quizcitbackend.modules.auth.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Token xac nhan khong duoc de trong")
        String confirmedToken,

        @NotBlank(message = "Mat khau khong duoc de trong")
        @Size(min = 6, max = 255, message = "Mat khau phai tu 6 den 255 ky tu")
        String newPassword
) {
}

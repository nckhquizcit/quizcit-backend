package vn.id.quiz.quizcitbackend.modules.user.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @NotBlank(message = "Ten dang nhap khong duoc de trong")
        @Size(max = 50, message = "Ten dang nhap khong duoc vuot qua 50 ky tu")
        String username,

        @NotBlank(message = "Email khong duoc de trong")
        @Email(message = "Email khong hop le")
        @Size(max = 100, message = "Email khong duoc vuot qua 100 ky tu")
        String email,

        @Size(max = 100, message = "Ten hien thi khong duoc vuot qua 100 ky tu")
        String displayName
) {
}

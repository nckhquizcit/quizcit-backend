package vn.id.quiz.quizcitbackend.modules.auth.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Ten dang nhap khong duoc de trong")
        @Size(max = 50, message = "Ten dang nhap khong duoc vuot qua 50 ky tu")
        String username,

        @NotBlank(message = "Mat khau khong duoc de trong")
        @Size(min = 6, max = 255, message = "Mat khau phai tu 6 den 255 ky tu")
        String password,

        @NotBlank(message = "Email khong duoc de trong")
        @Email(message = "Email khong hop le")
        @Size(max = 100, message = "Email khong duoc vuot qua 100 ky tu")
        String email,

        @Size(max = 100, message = "Ten hien thi khong duoc vuot qua 100 ky tu")
        String displayName
) {
}

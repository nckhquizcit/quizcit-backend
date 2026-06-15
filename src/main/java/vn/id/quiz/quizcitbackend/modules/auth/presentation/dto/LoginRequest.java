package vn.id.quiz.quizcitbackend.modules.auth.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Ten dang nhap hoac email khong duoc de trong")
        @Size(max = 100, message = "Ten dang nhap hoac email khong duoc vuot qua 100 ky tu")
        String identifier,

        @NotBlank(message = "Mat khau khong duoc de trong")
        @Size(min = 6, max = 255, message = "Mat khau phai tu 6 den 255 ky tu")
        String password
) {
}

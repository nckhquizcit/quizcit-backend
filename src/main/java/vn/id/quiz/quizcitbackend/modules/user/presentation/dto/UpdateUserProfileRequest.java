package vn.id.quiz.quizcitbackend.modules.user.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @NotBlank(message = "Ten hien thi khong duoc de trong")
        @Size(max = 100, message = "Ten hien thi khong duoc vuot qua 100 ky tu")
        String displayName
) {
}

package vn.id.quiz.quizcitbackend.modules.content.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record UploadUrlRequest(
        @NotBlank(message = "URL hình ảnh không được để trống")
        @URL(message = "URL hình ảnh không hợp lệ")
        String url,
        String folder
) {
}

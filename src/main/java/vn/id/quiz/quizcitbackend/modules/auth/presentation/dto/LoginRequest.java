package vn.id.quiz.quizcitbackend.modules.auth.presentation.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

public record LoginRequest(
        @JsonAlias({"username", "email"})
        @NotBlank(message = ErrorMessages.IDENTIFIER_REQUIRED)
        @Size(max = 100, message = ErrorMessages.IDENTIFIER_LENGTH)
        String identifier,

        @NotBlank(message = ErrorMessages.PASSWORD_REQUIRED)
        @Size(min = 6, max = 255, message = ErrorMessages.PASSWORD_LENGTH)
        String password
) {
}

package vn.id.quiz.quizcitbackend.modules.auth.presentation.dto;

import java.io.Serializable;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserStatus;

public record AuthUserState(
        String userId,
        UserStatus status,
        Long tokenVersion
) implements Serializable {}

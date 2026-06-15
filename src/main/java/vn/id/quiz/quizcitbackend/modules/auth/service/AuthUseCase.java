package vn.id.quiz.quizcitbackend.modules.auth.service;

import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.AuthResponse;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.LoginRequest;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.RegisterRequest;

public interface AuthUseCase {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}

package vn.id.quiz.quizcitbackend.modules.auth.service;

import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.AuthResponse;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.ForgotPasswordRequest;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.ForgotPasswordResponse;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.LoginRequest;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.RegisterRequest;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.RegisterResponse;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.ResendRequest;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.ResetPasswordRequest;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.VerifyRequest;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.VerifyResetOtpRequest;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.VerifyResetOtpResponse;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.dto.RefreshTokenRequest;

public interface AuthUseCase {
    RegisterResponse register(RegisterRequest request);

    AuthResponse verify(VerifyRequest request);

    RegisterResponse resend(ResendRequest request);

    AuthResponse login(LoginRequest request);

    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request);

    VerifyResetOtpResponse verifyResetOtp(VerifyResetOtpRequest request);

    AuthResponse resetPassword(ResetPasswordRequest request);
    AuthResponse refresh(RefreshTokenRequest request);
    void logout(RefreshTokenRequest request);
}

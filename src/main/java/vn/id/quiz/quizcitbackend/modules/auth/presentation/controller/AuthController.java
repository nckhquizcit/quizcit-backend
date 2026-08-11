package vn.id.quiz.quizcitbackend.modules.auth.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
import vn.id.quiz.quizcitbackend.modules.auth.service.AuthUseCase;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    private final AuthUseCase authUseCase;

    // Dang ky tai khoan moi: tao user UNVERIFIED, gui OTP qua email, tra ve token xac thuc.
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        RegisterResponse response = authUseCase.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(HttpStatus.CREATED, "Đăng ký tài khoản thành công", response));
    }

    // Xac thuc OTP: dung thi set VERIFIED va cap token dang nhap luon.
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verify(
            @Valid @RequestBody VerifyRequest request
    ) {
        AuthResponse response = authUseCase.verify(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Gui lai ma OTP moi cho tai khoan chua xac thuc.
    @PostMapping("/resend")
    public ResponseEntity<ApiResponse<RegisterResponse>> resend(
            @Valid @RequestBody ResendRequest request
    ) {
        RegisterResponse response = authUseCase.resend(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Dang nhap bang username/email va password trong bang users.
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authUseCase.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Quen mat khau buoc 1: gui OTP dat lai mat khau qua email, tra ve token dat lai.
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<ForgotPasswordResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        ForgotPasswordResponse response = authUseCase.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Quen mat khau buoc 2: xac thuc OTP, tra ve token xac nhan de dat lai mat khau.
    @PostMapping("/verify-reset-otp")
    public ResponseEntity<ApiResponse<VerifyResetOtpResponse>> verifyResetOtp(
            @Valid @RequestBody VerifyResetOtpRequest request
    ) {
        VerifyResetOtpResponse response = authUseCase.verifyResetOtp(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Quen mat khau buoc 3: dat mat khau moi va cap token dang nhap luon.
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<AuthResponse>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        AuthResponse response = authUseCase.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authUseCase.refresh(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authUseCase.logout(request);
        return ResponseEntity.noContent().build();
    }
}

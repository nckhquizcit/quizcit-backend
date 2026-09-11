package vn.id.quiz.quizcitbackend.modules.user.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.AccessMatrixResponse;
import vn.id.quiz.quizcitbackend.modules.user.service.UserUseCase;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

@RestController
@RequestMapping({"/api/admin/access", "/api/v1/admin/access"})
@RequiredArgsConstructor
public class AdminAccessController {

    private final UserUseCase userUseCase;

    @GetMapping({"/matrix", "/roles-and-permissions"})
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<AccessMatrixResponse>> getAccessMatrix() {
        return ResponseEntity.ok(ApiResponse.success(userUseCase.getAccessMatrix()));
    }
}

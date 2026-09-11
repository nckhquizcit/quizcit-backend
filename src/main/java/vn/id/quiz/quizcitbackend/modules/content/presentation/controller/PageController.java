package vn.id.quiz.quizcitbackend.modules.content.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.page.HomeSummaryResponse;
import vn.id.quiz.quizcitbackend.modules.content.service.PageService;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
import vn.id.quiz.quizcitbackend.shared.security.CustomUserPrincipal;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class PageController {

    private final PageService pageService;

    @GetMapping({"/pages/home-summary", "/v1/pages/home-summary"})
    public ResponseEntity<ApiResponse<HomeSummaryResponse>> getHomeSummary(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        if (principal == null || principal.getUserId() == null || principal.getUserId().isBlank()) {
            throw new AppException("Bạn cần đăng nhập để thực hiện thao tác này.", HttpStatus.UNAUTHORIZED);
        }
        HomeSummaryResponse response = pageService.getHomeSummary(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

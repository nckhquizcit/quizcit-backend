package vn.id.quiz.quizcitbackend.modules.ai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaRequest;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaResponse;
import vn.id.quiz.quizcitbackend.modules.ai.service.AIService;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

/**
 * Controller cho các API AI (IRT estimation).
 *
 * <p><b>Fix Bug 5 (Phase 1 - P2):</b> Trước đây controller ignore toàn bộ
 * request body và trả về {@code double} thô (không qua {@code ApiResponse}),
 * gây khó khăn cho frontend. Sau khi fix:
 * <ul>
 *   <li>Nhận {@link EstimateThetaRequest} làm Request Body với validation.</li>
 *   <li>Chuyển tiếp nguyên văn sang {@link AIClient}.</li>
 *   <li>Trả về {@link ApiResponse} chuẩn.</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AIService aiService;

    @PostMapping("/estimate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EstimateThetaResponse>> estimate(
            @Valid @RequestBody EstimateThetaRequest request) {

        EstimateThetaResponse response = aiService.estimateTheta(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

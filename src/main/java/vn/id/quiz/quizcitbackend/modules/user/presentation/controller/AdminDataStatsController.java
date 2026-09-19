package vn.id.quiz.quizcitbackend.modules.user.presentation.controller;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.AdminDataStatsResponse;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.AdminProfileSummaryResponse;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.UserResponse;
import vn.id.quiz.quizcitbackend.modules.user.service.UserUseCase;
import vn.id.quiz.quizcitbackend.shared.config.CacheConfig;
import vn.id.quiz.quizcitbackend.shared.security.CustomUserPrincipal;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

@RestController
@RequestMapping({"/api/admin", "/api/v1/admin"})
@RequiredArgsConstructor
public class AdminDataStatsController {
    private final EntityManager entityManager;
    private final UserUseCase userUseCase;

    @GetMapping("/data-stats")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_ADMIN_DATA_STATS, key = "'summary'")
    public ApiResponse<AdminDataStatsResponse> getDataStats() {
        Object[] row = (Object[]) entityManager.createNativeQuery("""
                SELECT
                  (SELECT COUNT(*) FROM users WHERE deleted_at IS NULL),
                  (SELECT COUNT(*) FROM subjects WHERE deleted_at IS NULL),
                  (SELECT COUNT(*) FROM questions WHERE deleted_at IS NULL),
                  (SELECT COUNT(*) FROM quizzes WHERE type <> 'ADAPTIVE' AND deleted_at IS NULL),
                  (SELECT COUNT(*) FROM attempts a
                     WHERE EXISTS (SELECT 1 FROM responses r WHERE r.attempt_id = a.attempt_id)
                       AND (NOT EXISTS (SELECT 1 FROM quizzes q
                                        WHERE q.quiz_id = a.quiz_id AND q.type = 'ADAPTIVE')
                            OR (SELECT COUNT(*) FROM responses r WHERE r.attempt_id = a.attempt_id)
                               >= a.total_question)),
                  (SELECT COUNT(*) FROM skills WHERE deleted_at IS NULL)
                """).getSingleResult();
        long subjectCount = number(row[1]);
        long actualSubjectCount = subjectCount > 0 ? subjectCount - 1 : 0;
        return ApiResponse.success(new AdminDataStatsResponse(
                number(row[0]), actualSubjectCount, number(row[2]), number(row[3]), number(row[4]), number(row[5])));
    }

    @GetMapping({"/profile", "/profile/me"})
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Transactional(readOnly = true)
    public ApiResponse<AdminProfileSummaryResponse> getAdminProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        UserResponse admin = (principal != null && principal.getUserId() != null && userUseCase != null)
                ? userUseCase.getUserById(principal.getUserId())
                : null;
        AdminDataStatsResponse stats = getDataStats().data();
        return ApiResponse.success(new AdminProfileSummaryResponse(admin, stats));
    }

    private long number(Object value) {
        return ((Number) value).longValue();
    }
}

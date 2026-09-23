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

    @GetMapping("/theta-trends")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Transactional(readOnly = true)
    public ApiResponse<java.util.List<vn.id.quiz.quizcitbackend.modules.user.presentation.dto.SubjectThetaTrendResponse>> getThetaTrends() {
        @SuppressWarnings("unchecked")
        java.util.List<Object[]> results = entityManager.createNativeQuery("""
                SELECT s.name, COALESCE(AVG(ah.delta), 0)
                FROM subjects s
                LEFT JOIN questions q ON q.subject_id = s.subject_id AND q.deleted_at IS NULL
                LEFT JOIN question_skills qs ON qs.question_id = q.question_id
                LEFT JOIN ability_history ah ON ah.skill_id = qs.skill_id
                WHERE s.deleted_at IS NULL AND s.is_visible = true
                GROUP BY s.subject_id, s.name, s.display_order
                ORDER BY s.display_order ASC
                """).getResultList();

        java.util.List<vn.id.quiz.quizcitbackend.modules.user.presentation.dto.SubjectThetaTrendResponse> response = results.stream()
                .map(row -> new vn.id.quiz.quizcitbackend.modules.user.presentation.dto.SubjectThetaTrendResponse(
                        (String) row[0],
                        row[1] != null ? ((Number) row[1]).doubleValue() : 0.0
                ))
                .toList();
        return ApiResponse.success(response);
    }

    @GetMapping("/theta-trends/subjects")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Transactional(readOnly = true)
    public ApiResponse<java.util.List<vn.id.quiz.quizcitbackend.modules.user.presentation.dto.SubjectOptionResponse>> getVisibleSubjectsForTrends() {
        @SuppressWarnings("unchecked")
        java.util.List<Object[]> results = entityManager.createNativeQuery("""
                SELECT s.subject_id, s.name
                FROM subjects s
                WHERE s.deleted_at IS NULL AND s.is_visible = true
                ORDER BY s.display_order ASC
                """).getResultList();

        java.util.List<vn.id.quiz.quizcitbackend.modules.user.presentation.dto.SubjectOptionResponse> response = results.stream()
                .map(row -> new vn.id.quiz.quizcitbackend.modules.user.presentation.dto.SubjectOptionResponse((String) row[0], (String) row[1]))
                .toList();
        return ApiResponse.success(response);
    }

    @GetMapping("/theta-trends/subjects/{subjectId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Transactional(readOnly = true)
    public ApiResponse<java.util.List<vn.id.quiz.quizcitbackend.modules.user.presentation.dto.TimeSeriesThetaTrendResponse>> getSubjectThetaTrendOverTime(
            @org.springframework.web.bind.annotation.PathVariable String subjectId,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "3") int months
    ) {
        String truncateUnit = "week";
        String nativeQuery = """
                SELECT TO_CHAR(d.date_val, 'DD-MM-YY'), AVG(subj_ah.theta)
                FROM (
                  SELECT generate_series(
                    DATE_TRUNC('%s', CURRENT_DATE - INTERVAL '1 month' * :m),
                    DATE_TRUNC('%s', CURRENT_DATE),
                    '1 %s'::interval
                  ) as date_val
                ) d
                LEFT JOIN (
                  SELECT ah.created_at, ah.theta
                  FROM ability_history ah
                  JOIN question_skills qs ON qs.skill_id = ah.skill_id
                  JOIN questions q ON q.question_id = qs.question_id
                  WHERE q.subject_id = :subj
                ) subj_ah ON DATE_TRUNC('%s', subj_ah.created_at) = d.date_val
                GROUP BY d.date_val
                ORDER BY d.date_val ASC
                """.formatted(truncateUnit, truncateUnit, truncateUnit, truncateUnit);

        @SuppressWarnings("unchecked")
        java.util.List<Object[]> results = entityManager.createNativeQuery(nativeQuery)
                .setParameter("subj", subjectId)
                .setParameter("m", months)
                .getResultList();

        java.util.List<vn.id.quiz.quizcitbackend.modules.user.presentation.dto.TimeSeriesThetaTrendResponse> response = results.stream()
                .map(row -> new vn.id.quiz.quizcitbackend.modules.user.presentation.dto.TimeSeriesThetaTrendResponse(
                        (String) row[0],
                        row[1] != null ? ((Number) row[1]).doubleValue() : null
                ))
                .toList();
        return ApiResponse.success(response);
    }

    private long number(Object value) {
        return ((Number) value).longValue();
    }
}

package vn.id.quiz.quizcitbackend.modules.user.presentation.controller;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.id.quiz.quizcitbackend.modules.user.presentation.dto.AdminDataStatsResponse;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminDataStatsController {
    private final EntityManager entityManager;

    @GetMapping("/data-stats")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Transactional(readOnly = true)
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
                               >= a.total_question))
                """).getSingleResult();
        return ApiResponse.success(new AdminDataStatsResponse(
                number(row[0]), number(row[1]), number(row[2]), number(row[3]), number(row[4])));
    }

    private long number(Object value) {
        return ((Number) value).longValue();
    }
}

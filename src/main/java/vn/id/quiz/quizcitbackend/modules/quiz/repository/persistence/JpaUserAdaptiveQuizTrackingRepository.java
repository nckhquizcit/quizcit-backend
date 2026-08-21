package vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.UserAdaptiveQuizTracking;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.UserAdaptiveQuizTrackingId;

import java.util.Optional;

public interface JpaUserAdaptiveQuizTrackingRepository
        extends JpaRepository<UserAdaptiveQuizTracking, UserAdaptiveQuizTrackingId> {

    Optional<UserAdaptiveQuizTracking> findByUserIdAndSubjectId(String userId, String subjectId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO user_adaptive_quiz_trackings (user_id, subject_id, completed_standard_quizzes, required_standard_quizzes, updated_at)
            VALUES (:userId, :subjectId, 1, 2, CURRENT_TIMESTAMP)
            ON CONFLICT (user_id, subject_id) DO UPDATE
            SET completed_standard_quizzes = user_adaptive_quiz_trackings.completed_standard_quizzes + 1,
                updated_at = CURRENT_TIMESTAMP
            """, nativeQuery = true)
    void incrementCompletedStandardQuizzes(@Param("userId") String userId, @Param("subjectId") String subjectId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO user_adaptive_quiz_trackings (user_id, subject_id, completed_standard_quizzes, required_standard_quizzes, last_adaptive_completed_at, updated_at)
            VALUES (:userId, :subjectId, 0, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (user_id, subject_id) DO UPDATE
            SET completed_standard_quizzes = 0,
                last_adaptive_completed_at = CURRENT_TIMESTAMP,
                updated_at = CURRENT_TIMESTAMP
            """, nativeQuery = true)
    void resetCompletedStandardQuizzes(@Param("userId") String userId, @Param("subjectId") String subjectId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM UserAdaptiveQuizTracking t WHERE t.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
}

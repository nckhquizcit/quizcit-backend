package vn.id.quiz.quizcitbackend.modules.content.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.UserCourseEnrollment;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.UserCourseEnrollmentId;

import java.util.List;

public interface JpaUserCourseEnrollmentRepository
        extends JpaRepository<UserCourseEnrollment, UserCourseEnrollmentId> {

    boolean existsByUserIdAndSubjectId(String userId, String subjectId);

    @Query("""
            SELECT enrollment.subjectId FROM UserCourseEnrollment enrollment
            WHERE enrollment.userId = :userId
            """)
    List<String> findSubjectIdsByUserId(@Param("userId") String userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO user_course_enrollments (user_id, subject_id)
            VALUES (:userId, :subjectId)
            ON CONFLICT (user_id, subject_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("userId") String userId, @Param("subjectId") String subjectId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            DELETE FROM UserCourseEnrollment enrollment
            WHERE enrollment.userId = :userId AND enrollment.subjectId = :subjectId
            """)
    int deleteEnrollment(@Param("userId") String userId, @Param("subjectId") String subjectId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM UserCourseEnrollment enrollment WHERE enrollment.subjectId = :subjectId")
    void deleteBySubjectId(@Param("subjectId") String subjectId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM UserCourseEnrollment enrollment WHERE enrollment.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);

    @Query("""
            SELECT enrollment FROM UserCourseEnrollment enrollment
            JOIN FETCH enrollment.subject subject
            WHERE enrollment.userId = :userId
            ORDER BY subject.displayOrder ASC, subject.createdAt ASC, subject.subjectId ASC
            """)
    List<UserCourseEnrollment> findAllByUserIdWithSubject(@Param("userId") String userId);
}

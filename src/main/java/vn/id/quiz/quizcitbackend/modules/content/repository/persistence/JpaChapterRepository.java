package vn.id.quiz.quizcitbackend.modules.content.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.FlatChapterResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;

import java.util.List;
import java.util.Optional;
import java.util.Collection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaChapterRepository extends JpaRepository<Chapter, String> {
    List<Chapter> findBySubjectIdOrderByDisplayOrderAscCreatedAtAscChapterIdAsc(String subjectId);
    List<Chapter> findBySubjectIdInOrderBySubjectIdAscDisplayOrderAscCreatedAtAscChapterIdAsc(Collection<String> subjectIds);
    Optional<Chapter> findBySubjectIdAndName(String subjectId, String name);
    Chapter findTopBySubjectIdOrderByDisplayOrderDesc(String subjectId);

    @Query("""
            SELECT chapter FROM Chapter chapter
            JOIN Subject subject ON subject.subjectId = chapter.subjectId
            JOIN UserCourseEnrollment enrollment ON enrollment.subjectId = subject.subjectId
            WHERE enrollment.userId = :userId AND subject.visible = true
            ORDER BY subject.displayOrder ASC, subject.createdAt ASC, subject.subjectId ASC,
                     chapter.displayOrder ASC, chapter.createdAt ASC, chapter.chapterId ASC
            """)
    List<Chapter> findAccessibleByUserId(@Param("userId") String userId);

    @Query("""
            SELECT new vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.FlatChapterResponse(
                c.chapterId, c.name, s.subjectId, s.name, c.displayOrder
            )
            FROM Chapter c
            JOIN Subject s ON s.subjectId = c.subjectId
            WHERE s.courseCode <> 'QUESTION_BANK'
              AND s.name <> 'Ngân hàng câu hỏi'
            ORDER BY s.displayOrder ASC, s.createdAt ASC, s.subjectId ASC,
                     c.displayOrder ASC, c.createdAt ASC, c.chapterId ASC
            """)
    List<FlatChapterResponse> findAllFlatChapters();
}

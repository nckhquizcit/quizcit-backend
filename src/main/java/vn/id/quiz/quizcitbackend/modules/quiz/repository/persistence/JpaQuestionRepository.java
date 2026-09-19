package vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.ChapterCountProjection;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

//Mặc định các Repository extend Jpa là đã có sẵn CRUD và save, delete,findbyID và findAll
public interface JpaQuestionRepository extends JpaRepository<Question, String> {
    // JPA tự parse câu dưới thành dạng sql query
    List<Question> findByChapterId(String chapterId);
    List<Question> findByChapterIdIn(Collection<String> chapterIds);
    List<Question> findBySubjectId(String subjectId);
    List<Question> findBySubjectIdAndChapterId(String subjectId, String chapterId);

    Page<Question> findBySubjectId(String subjectId, Pageable pageable);
    Page<Question> findByChapterId(String chapterId, Pageable pageable);
    Page<Question> findBySubjectIdAndChapterId(String subjectId, String chapterId, Pageable pageable);

    @Query("SELECT q.chapterId AS chapterId, COUNT(q) AS count FROM Question q WHERE q.chapterId IN :chapterIds GROUP BY q.chapterId")
    List<ChapterCountProjection> countQuestionsByChapterIds(@Param("chapterIds") Collection<String> chapterIds);

    Optional<Question> findByQuestionId(
            String questionId);

    // Batch: lấy nhiều câu hỏi trong 1 truy vấn (tránh N+1).
    List<Question> findByQuestionIdIn(Collection<String> questionIds);
    
    @Query("""
        SELECT q
        FROM Question q
        WHERE q.chapterId IN :chapterIds
        AND (SELECT COUNT(o) FROM Option o WHERE o.questionId = q.questionId) >= 2
        AND (SELECT COUNT(o) FROM Option o WHERE o.questionId = q.questionId AND o.isCorrected = true) >= 1
    """)
    List<Question> findAnswerableQuestionsByChapterIds(@Param("chapterIds") Collection<String> chapterIds);

    @Query("""
            select q from Question q, QuizQuestion qq
            where qq.quizId = :quizId and qq.questionId = q.questionId
            order by qq.indexOrder
            """)
    List<Question> findByQuizIdOrderByQuizIndex(@Param("quizId") String quizId);

    void deleteByQuestionId(
            String questionId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Question q SET q.subjectId = :targetSubjectId, q.chapterId = :targetChapterId WHERE q.subjectId = :subjectId")
    void reassignQuestionsBySubjectId(
            @Param("subjectId") String subjectId,
            @Param("targetSubjectId") String targetSubjectId,
            @Param("targetChapterId") String targetChapterId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Question q SET q.subjectId = :targetSubjectId, q.chapterId = :targetChapterId WHERE q.chapterId = :chapterId")
    void reassignQuestionsByChapterId(
            @Param("chapterId") String chapterId,
            @Param("targetSubjectId") String targetSubjectId,
            @Param("targetChapterId") String targetChapterId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Question q SET q.subjectId = :targetSubjectId, q.chapterId = :targetChapterId WHERE q.chapterId IN :chapterIds")
    void reassignQuestionsByChapterIds(
            @Param("chapterIds") Collection<String> chapterIds,
            @Param("targetSubjectId") String targetSubjectId,
            @Param("targetChapterId") String targetChapterId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Question q SET q.subjectId = NULL, q.chapterId = NULL WHERE q.subjectId = :subjectId")
    void detachQuestionsBySubjectId(@Param("subjectId") String subjectId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Question q SET q.subjectId = NULL, q.chapterId = NULL WHERE q.chapterId = :chapterId")
    void detachQuestionsByChapterId(@Param("chapterId") String chapterId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Question q SET q.subjectId = NULL, q.chapterId = NULL WHERE q.chapterId IN :chapterIds")
    void detachQuestionsByChapterIds(@Param("chapterIds") Collection<String> chapterIds);
}

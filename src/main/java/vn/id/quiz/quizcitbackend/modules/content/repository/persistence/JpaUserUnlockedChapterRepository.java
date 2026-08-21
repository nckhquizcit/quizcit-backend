package vn.id.quiz.quizcitbackend.modules.content.repository.persistence;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.UserUnlockedChapter;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.UserUnlockedChapterId;

public interface JpaUserUnlockedChapterRepository extends JpaRepository<UserUnlockedChapter, UserUnlockedChapterId> {
    boolean existsByUserIdAndChapterId(String userId, String chapterId);
    List<UserUnlockedChapter> findByUserIdAndChapterIdIn(String userId, Collection<String> chapterIds);

    /**
     * Mở khóa theo kiểu idempotent. Hai request đồng thời hoặc một lần đánh giá
     * lặp lại cùng chương đều không được biến thành lỗi duplicate key.
     */
    @Modifying(flushAutomatically = true)
    @Query(value = """
            INSERT INTO user_unlocked_chapters (user_id, chapter_id)
            VALUES (:userId, :chapterId)
            ON CONFLICT (user_id, chapter_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(
            @Param("userId") String userId,
            @Param("chapterId") String chapterId);

    @Modifying(flushAutomatically = true)
    @Query(value = """
            INSERT INTO user_unlocked_chapters (user_id, chapter_id)
            SELECT :userId, chapter.chapter_id
            FROM chapters chapter
            WHERE chapter.chapter_id IN (:chapterIds) AND chapter.deleted_at IS NULL
            ON CONFLICT (user_id, chapter_id) DO NOTHING
            """, nativeQuery = true)
    int insertAllIfAbsent(
            @Param("userId") String userId,
            @Param("chapterIds") Collection<String> chapterIds);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM UserUnlockedChapter u WHERE u.chapterId = :chapterId")
    void deleteByChapterId(@Param("chapterId") String chapterId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM UserUnlockedChapter u WHERE u.chapterId IN :chapterIds")
    void deleteByChapterIdIn(@Param("chapterIds") Collection<String> chapterIds);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM UserUnlockedChapter u WHERE u.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
}

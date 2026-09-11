package vn.id.quiz.quizcitbackend.modules.content.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Theory;

import java.util.Collection;
import java.util.List;

public interface JpaTheoryRepository extends JpaRepository<Theory, String> {
    List<Theory> findByChapterId(String chapterId);
    List<Theory> findByChapterIdIn(Collection<String> chapterIds);

    @Query("SELECT t.chapterId AS chapterId, COUNT(t) AS count FROM Theory t WHERE t.chapterId IN :chapterIds GROUP BY t.chapterId")
    List<ChapterCountProjection> countTheoriesByChapterIds(@Param("chapterIds") Collection<String> chapterIds);
}

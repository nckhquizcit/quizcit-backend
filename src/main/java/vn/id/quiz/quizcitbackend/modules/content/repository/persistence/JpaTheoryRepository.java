package vn.id.quiz.quizcitbackend.modules.content.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Theory;

import java.util.List;
import java.util.Collection;

public interface JpaTheoryRepository extends JpaRepository<Theory, String> {
    List<Theory> findByChapterId(String chapterId);
    List<Theory> findByChapterIdIn(Collection<String> chapterIds);
}

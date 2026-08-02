package vn.id.quiz.quizcitbackend.modules.content.repository.persistence;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.UserUnlockedChapter;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.UserUnlockedChapterId;

public interface JpaUserUnlockedChapterRepository extends JpaRepository<UserUnlockedChapter, UserUnlockedChapterId> {
    boolean existsByUserIdAndChapterId(String userId, String chapterId);
    List<UserUnlockedChapter> findByUserIdAndChapterIdIn(String userId, Collection<String> chapterIds);
}

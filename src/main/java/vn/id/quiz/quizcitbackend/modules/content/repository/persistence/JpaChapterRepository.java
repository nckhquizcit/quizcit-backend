package vn.id.quiz.quizcitbackend.modules.content.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;

import java.util.List;
import java.util.Collection;

public interface JpaChapterRepository extends JpaRepository<Chapter, String> {
    List<Chapter> findBySubjectIdOrderByDisplayOrderAscCreatedAtAscChapterIdAsc(String subjectId);
    List<Chapter> findBySubjectIdInOrderBySubjectIdAscDisplayOrderAscCreatedAtAscChapterIdAsc(Collection<String> subjectIds);
    Chapter findTopBySubjectIdOrderByDisplayOrderDesc(String subjectId);
}

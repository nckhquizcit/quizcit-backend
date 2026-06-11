package vn.id.quiz.quizcitbackend.modules.content.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;

import java.util.List;

public interface JpaChapterRepository extends JpaRepository<Chapter, String> {
    List<Chapter> findBySubjectId(String subjectId);
}

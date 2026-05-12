package vn.id.quiz.quizcitbackend.modules.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Chapter;

public interface JpaChapterRepository extends JpaRepository<Chapter, String> {
}

package vn.id.quiz.quizcitbackend.modules.content.repository.persistence;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.ChapterSkill;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.ChapterSkillId;

public interface JpaChapterSkillRepository extends JpaRepository<ChapterSkill, ChapterSkillId> {
    List<ChapterSkill> findByChapterId(String chapterId);
    boolean existsBySkillId(String skillId);
    List<ChapterSkill> findByChapterIdIn(Collection<String> chapterIds);
    void deleteByChapterId(String chapterId);
    void deleteByChapterIdIn(Collection<String> chapterIds);
    void deleteBySkillId(String skillId);

    @Query(value = """
        SELECT EXISTS (
          SELECT 1 FROM questions q JOIN question_skills qs ON qs.question_id = q.question_id
          WHERE q.chapter_id = :chapterId AND qs.skill_id IN (:skillIds) AND q.deleted_at IS NULL
        )
        """, nativeQuery = true)
    boolean existsQuestionUsingRemovedSkills(String chapterId, Collection<String> skillIds);
}

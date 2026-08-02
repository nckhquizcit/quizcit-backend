package vn.id.quiz.quizcitbackend.modules.content.repository;

import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Theory;

import java.util.List;
import java.util.Optional;


public interface ContentRepository {
    List<Subject> findAllSubject();
    default List<Subject> findAllVisibleSubject() {
        return findAllSubject().stream().filter(Subject::isVisible).toList();
    }
    List<Chapter> findAllChapter(String subjectId);
    List<Theory> findAllTheory(String chapterId);
    default int nextSubjectDisplayOrder() { return 1; }
    default int nextChapterDisplayOrder(String subjectId) { return 1; }

    Subject save(Subject subject);
    Chapter save(Chapter chapter);
    Theory save(Theory theory);

    Optional<Subject> findSubjectById(String id);
    Optional<Theory> findTheoryById(String id);
    Optional<Chapter> findChapterById(String id);

    boolean existsByCourseCode(String courseCode);
    boolean existsBySubjectId(String subjectId);
    boolean existsByChapterId(String chapterId);
    boolean existsByTheoryId(String theoryId);


    void deleteSubjectById(String subjectId);
    void deleteChapterById(String chapterId);
    void deleteTheoryById(String theoryId);


}

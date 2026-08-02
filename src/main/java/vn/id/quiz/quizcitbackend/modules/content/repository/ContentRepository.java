package vn.id.quiz.quizcitbackend.modules.content.repository;

import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Theory;

import java.util.List;
import java.util.Optional;
import java.util.Collection;


public interface ContentRepository {
    List<Subject> findAllSubject();
    default List<Subject> findAllVisibleSubject() {
        return findAllSubject().stream().filter(Subject::isVisible).toList();
    }
    List<Chapter> findAllChapter(String subjectId);
    default List<Chapter> findAllChapters(Collection<String> subjectIds) {
        return subjectIds == null ? List.of() : subjectIds.stream()
                .flatMap(id -> findAllChapter(id).stream()).toList();
    }
    List<Theory> findAllTheory(String chapterId);
    default List<Theory> findAllTheories(Collection<String> chapterIds) {
        return chapterIds == null ? List.of() : chapterIds.stream()
                .flatMap(id -> findAllTheory(id).stream()).toList();
    }
    default int nextSubjectDisplayOrder() { return 1; }
    default int nextChapterDisplayOrder(String subjectId) { return 1; }

    Subject save(Subject subject);
    Chapter save(Chapter chapter);
    Theory save(Theory theory);

    Optional<Subject> findSubjectById(String id);
    default List<Subject> findSubjectsByIds(Collection<String> ids) {
        return ids == null ? List.of() : ids.stream().map(this::findSubjectById)
                .flatMap(Optional::stream).toList();
    }
    Optional<Theory> findTheoryById(String id);
    Optional<Chapter> findChapterById(String id);
    default List<Chapter> findChaptersByIds(Collection<String> ids) {
        return ids == null ? List.of() : ids.stream().map(this::findChapterById)
                .flatMap(Optional::stream).toList();
    }

    boolean existsByCourseCode(String courseCode);
    boolean existsBySubjectId(String subjectId);
    boolean existsByChapterId(String chapterId);
    boolean existsByTheoryId(String theoryId);


    void deleteSubjectById(String subjectId);
    void deleteChapterById(String chapterId);
    void deleteTheoryById(String theoryId);


}

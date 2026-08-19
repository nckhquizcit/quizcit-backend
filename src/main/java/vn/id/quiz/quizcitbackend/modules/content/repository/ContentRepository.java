package vn.id.quiz.quizcitbackend.modules.content.repository;

import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Theory;

import java.util.List;
import java.util.Optional;
import java.util.Collection;
import java.util.Map;


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
    default List<Chapter> findAccessibleChapters(String userId) { return List.of(); }
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
    default void updateSubjectDisplayOrders(Map<String, Integer> displayOrders) {
        if (displayOrders == null) return;
        displayOrders.forEach((id, order) -> findSubjectById(id).ifPresent(subject -> {
            subject.updateDisplayOrder(order);
            save(subject);
        }));
    }
    default void updateChapterDisplayOrders(Map<String, Integer> displayOrders) {
        if (displayOrders == null) return;
        displayOrders.forEach((id, order) -> findChapterById(id).ifPresent(chapter -> {
            chapter.updateDisplayOrder(order);
            save(chapter);
        }));
    }

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

    default Optional<Subject> findSubjectByCourseCode(String courseCode) {
        return Optional.empty();
    }

    default Optional<Chapter> findChapterBySubjectIdAndName(String subjectId, String name) {
        return Optional.empty();
    }

    default Subject getOrCreateQuestionBankSubject() {
        return findSubjectByCourseCode(vn.id.quiz.quizcitbackend.modules.content.service.ContentConstants.QUESTION_BANK_COURSE_CODE)
                .orElseGet(() -> {
                    int displayOrder = nextSubjectDisplayOrder();
                    Subject bankSubject = Subject.create(
                            vn.id.quiz.quizcitbackend.modules.content.service.ContentConstants.QUESTION_BANK_COURSE_CODE,
                            vn.id.quiz.quizcitbackend.modules.content.service.ContentConstants.QUESTION_BANK_NAME,
                            vn.id.quiz.quizcitbackend.modules.content.service.ContentConstants.QUESTION_BANK_DESCRIPTION,
                            displayOrder
                    );
                    return save(bankSubject);
                });
    }

    default Chapter getOrCreateQuestionBankChapter(String subjectId) {
        return findChapterBySubjectIdAndName(subjectId, vn.id.quiz.quizcitbackend.modules.content.service.ContentConstants.QUESTION_BANK_CHAPTER_NAME)
                .orElseGet(() -> {
                    int displayOrder = nextChapterDisplayOrder(subjectId);
                    Chapter bankChapter = Chapter.create(
                            subjectId,
                            vn.id.quiz.quizcitbackend.modules.content.service.ContentConstants.QUESTION_BANK_CHAPTER_NAME,
                            displayOrder
                    );
                    return save(bankChapter);
                });
    }

    void deleteSubjectById(String subjectId);
    void deleteChapterById(String chapterId);
    void deleteTheoryById(String theoryId);


}

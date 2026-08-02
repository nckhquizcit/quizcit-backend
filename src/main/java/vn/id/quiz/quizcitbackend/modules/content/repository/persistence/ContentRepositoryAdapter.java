package vn.id.quiz.quizcitbackend.modules.content.repository.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Theory;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ContentRepositoryAdapter implements ContentRepository {
    private final JpaSubjectRepository jpaSubjectRepository;
    private final JpaChapterRepository jpaChapterRepository;
    private final JpaTheoryRepository jpaTheoryRepository;


    @Override
    public List<Subject> findAllSubject(){return jpaSubjectRepository.findAllByOrderByDisplayOrderAscCreatedAtAscSubjectIdAsc();}

    @Override
    public List<Subject> findAllVisibleSubject(){return jpaSubjectRepository.findByVisibleTrueOrderByDisplayOrderAscCreatedAtAscSubjectIdAsc();}

    @Override
    public List<Chapter> findAllChapter(String subjectId) {
        return jpaChapterRepository.findBySubjectIdOrderByDisplayOrderAscCreatedAtAscChapterIdAsc(subjectId);
    }

    @Override
    public List<Chapter> findAllChapters(java.util.Collection<String> subjectIds) {
        return subjectIds == null || subjectIds.isEmpty() ? List.of()
                : jpaChapterRepository.findBySubjectIdInOrderBySubjectIdAscDisplayOrderAscCreatedAtAscChapterIdAsc(subjectIds);
    }

    @Override
    public List<Theory> findAllTheory(String chapterId) {
        return jpaTheoryRepository.findByChapterId(chapterId);
    }

    @Override
    public List<Theory> findAllTheories(java.util.Collection<String> chapterIds) {
        return chapterIds == null || chapterIds.isEmpty() ? List.of() : jpaTheoryRepository.findByChapterIdIn(chapterIds);
    }

    @Override
    public int nextSubjectDisplayOrder() {
        Subject last = jpaSubjectRepository.findTopByOrderByDisplayOrderDesc();
        return last == null ? 1 : last.getDisplayOrder() + 1;
    }

    @Override
    public int nextChapterDisplayOrder(String subjectId) {
        Chapter last = jpaChapterRepository.findTopBySubjectIdOrderByDisplayOrderDesc(subjectId);
        return last == null ? 1 : last.getDisplayOrder() + 1;
    }

    @Override
    public Subject save(Subject subject) { return jpaSubjectRepository.save(subject); }

    @Override
    public Chapter save(Chapter chapter) {
        return jpaChapterRepository.save(chapter);
    }

    @Override
    public Theory save(Theory theory) {
        return jpaTheoryRepository.save(theory);
    }

    @Override
    public Optional<Subject> findSubjectById(String id) { return jpaSubjectRepository.findById(id); }

    @Override
    public List<Subject> findSubjectsByIds(java.util.Collection<String> ids) {
        return ids == null || ids.isEmpty() ? List.of() : jpaSubjectRepository.findAllById(ids);
    }


    @Override
    public boolean existsByCourseCode(String courseCode) {return jpaSubjectRepository.existsByCourseCode(courseCode);}

    @Override
    public boolean existsBySubjectId(String subjectId){return jpaSubjectRepository.existsById(subjectId);}

    @Override
    public boolean existsByChapterId(String chapterId) {
        return jpaChapterRepository.existsById(chapterId);
    }

    @Override
    public boolean existsByTheoryId(String theoryId) {
        return jpaTheoryRepository.existsById(theoryId);
    }

    @Override
    public void deleteSubjectById(String subjectId){
        jpaSubjectRepository.deleteById(subjectId);}

    @Override
    public void deleteChapterById(String chapterId) {
        jpaChapterRepository.deleteById(chapterId);
    }

    @Override
    public void deleteTheoryById(String theoryId) {
        jpaTheoryRepository.deleteById(theoryId);
    }




    @Override
    public Optional<Theory> findTheoryById(String id) {
        return jpaTheoryRepository.findById(id);
    }

    @Override
    public Optional<Chapter> findChapterById(String id) {
        return jpaChapterRepository.findById(id);
    }

    @Override
    public List<Chapter> findChaptersByIds(java.util.Collection<String> ids) {
        return ids == null || ids.isEmpty() ? List.of() : jpaChapterRepository.findAllById(ids);
    }
}

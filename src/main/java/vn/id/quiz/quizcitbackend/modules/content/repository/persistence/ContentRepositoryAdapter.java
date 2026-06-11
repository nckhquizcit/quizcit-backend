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
    public List<Subject> findAllSubject(){return jpaSubjectRepository.findAll();}

    @Override
    public List<Chapter> findAllChapter(String subjectId) {
        return jpaChapterRepository.findBySubjectId(subjectId);
    }

    @Override
    public List<Theory> findAllTheory(String chapterId) {
        return jpaTheoryRepository.findByChapterId(chapterId);
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
    public List<Theory> findAllTheoriesBySubjectId(String subjectId){return jpaSubjectRepository.findAllTheoriesBySubjectId(subjectId);}


    @Override
    public Optional<Theory> findTheoryById(String id) {
        return jpaTheoryRepository.findById(id);
    }

    @Override
    public Optional<Chapter> findChapterById(String id) {
        return jpaChapterRepository.findById(id);
    }
}

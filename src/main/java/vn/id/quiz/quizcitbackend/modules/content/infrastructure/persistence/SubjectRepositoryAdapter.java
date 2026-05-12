package vn.id.quiz.quizcitbackend.modules.content.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Subject;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Theory;
import vn.id.quiz.quizcitbackend.modules.content.domain.repository.SubjectRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SubjectRepositoryAdapter implements SubjectRepository {
    private final JpaSubjectRepository jpaSubjectRepository;
    private final JpaChapterRepository jpaChapterRepository;
    private final JpaTheoryRepository jpaTheoryRepository;


    @Override
    public List<Subject> findAll(){return jpaSubjectRepository.findAll();}
    @Override
    public Subject save(Subject subject) { return jpaSubjectRepository.save(subject); }

    @Override
    public Optional<Subject> findSubjectById(String id) { return jpaSubjectRepository.findById(id); }

    @Override
    public void delete(Subject subject) { jpaSubjectRepository.delete(subject); }

    @Override
    public boolean existsByCourseCode(String courseCode) {return jpaSubjectRepository.existsByCourseCode(courseCode);}

    @Override
    public boolean existsById(String subjectId){return jpaSubjectRepository.existsById(subjectId);}

    @Override
    public void deleteById(String subjectId){
        jpaSubjectRepository.deleteById(subjectId);}

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

package vn.id.quiz.quizcitbackend.modules.content.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Subject;
import vn.id.quiz.quizcitbackend.modules.content.domain.repository.SubjectRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SubjectRepositoryAdapter implements SubjectRepository {
    private final JpaSubjectRepository jpaRepository;


    @Override
    public Subject save(Subject subject) { return jpaRepository.save(subject); }

    @Override
    public Optional<Subject> findById(String id) { return jpaRepository.findById(id); }

    @Override
    public void delete(Subject subject) { jpaRepository.delete(subject); }

    @Override
    public boolean existByCourseCode(String courseCode) {return jpaRepository.existsByCourseCode(courseCode);}
}

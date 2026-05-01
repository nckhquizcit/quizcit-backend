package vn.id.quiz.quizcitbackend.modules.content.domain.repository;

import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Subject;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository {
    Optional<Subject> findById(String id);
    List<Subject> findAll();
    Subject save(Subject subject);
    void deleteById(String id);
    boolean existsById(String id);

}

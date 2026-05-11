package vn.id.quiz.quizcitbackend.modules.content.domain.repository;

import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Subject;

import java.util.Optional;


public interface SubjectRepository {
    Subject save(Subject subject);
    Optional<Subject> findById(String id);
    void delete(Subject subject);
    boolean existByCourseCode(String courseCode);

}

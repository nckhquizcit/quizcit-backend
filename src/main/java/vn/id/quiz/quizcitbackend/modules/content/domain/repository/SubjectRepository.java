package vn.id.quiz.quizcitbackend.modules.content.domain.repository;

import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Subject;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Theory;

import java.util.List;
import java.util.Optional;


public interface SubjectRepository {
    List<Subject> findAll();
    Subject save(Subject subject);
    Optional<Subject> findSubjectById(String id);
    Optional<Theory> findTheoryById(String id);
    Optional<Chapter> findChapterById(String id);

    void delete(Subject subject);
    boolean existsByCourseCode(String courseCode);
    boolean existsById(String subjectId);
    void deleteById(String subjectId);
    List<Theory> findAllTheoriesBySubjectId(String subjectId);

}

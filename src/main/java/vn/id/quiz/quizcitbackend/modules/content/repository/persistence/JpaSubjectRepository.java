package vn.id.quiz.quizcitbackend.modules.content.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Theory;

import java.util.List;

public interface JpaSubjectRepository extends JpaRepository<Subject, String> {
    List<Subject> findByVisibleTrue();
    boolean existsByCourseCode(String courseCode);
}


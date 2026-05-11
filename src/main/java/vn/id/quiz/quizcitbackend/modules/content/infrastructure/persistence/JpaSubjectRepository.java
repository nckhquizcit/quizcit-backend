package vn.id.quiz.quizcitbackend.modules.content.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Subject;
import vn.id.quiz.quizcitbackend.modules.content.domain.repository.SubjectRepository;

import java.util.Optional;

public interface JpaSubjectRepository extends JpaRepository<Subject, String> {
    boolean existsByCourseCode(String courseCode);
}


package vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Response;

import java.util.List;
import java.util.Collection;

public interface JpaResponseRepository extends JpaRepository<Response, String> {

    List<Response> findByAttemptId(String attemptId);
    List<Response> findByAttemptIdIn(Collection<String> attemptIds);

    void deleteByQuestionId(String questionId);

}

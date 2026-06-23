package vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;

import java.util.List;
import java.util.Optional;

//Mặc định các Repository extend Jpa là đã có sẵn CRUD và save, delete,findbyID và findAll
public interface JpaQuestionRepository extends JpaRepository<Question, String> {
    // JPA tự parse câu dưới thành dạng sql query
    List<Question> findByChapterId(String chapterId);

    Optional<Question> findByQuestionId(
            String questionId);

    void deleteByQuestionId(
            String questionId);
};
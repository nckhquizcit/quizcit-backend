package vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;

public interface JpaQuizRepository extends JpaRepository<Quiz, String>{
    //Hàm truy vấn cơ bản
    Optional<Quiz> findByQuizId(
            String quizId);

    List<Quiz> findByChapterChapterId(
            String chapterId);

    void deleteByQuizId(
            String quizId);
}

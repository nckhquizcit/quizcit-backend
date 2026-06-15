package vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizQuestion;

public interface JpaQuizQuestionRepository
        extends JpaRepository<QuizQuestion, String> {
            List<QuizQuestion> findByQuizQuizIdOrderByIndexOrder(String quizId);

            void deleteByQuizQuizId(
                    String quizId);
}
package vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizQuestion;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizQuestionId;

public interface JpaQuizQuestionRepository
        extends JpaRepository<QuizQuestion, QuizQuestionId> {
            List<QuizQuestion> findByQuizIdOrderByIndexOrder(String quizId);

            void deleteByQuestionId(
                    String questionId);

            void deleteByQuizId(
                    String quizId);
}
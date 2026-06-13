package vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizQuestion;

public interface QuizQuestionRepository
        extends JpaRepository<QuizQuestion, String> {
}
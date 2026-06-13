package vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizQuestion;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QuizRepositoryAdapter implements QuizRepository {

    private final JpaQuestionRepository jpaQuestionRepository;
    private final JpaQuizRepository jpaQuizRepository;
    private final JpaQuizQuestionRepository jpaQuizQuestionRepository;

    @Override
    public List<Question> findQuestionsByChapterId(String chapterId) {
        return jpaQuestionRepository.findByChapterChapterId(chapterId);
    }

    @Override
    public Quiz save(Quiz quiz) {
        return jpaQuizRepository.save(quiz);
    }

    @Override
    public QuizQuestion save(QuizQuestion quizQuestion) {
        return jpaQuizQuestionRepository.save(quizQuestion);
    }

}

package vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Attempt;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Option;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizQuestion;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Response;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;

import java.util.List;

//Adapter đóng vai trò là cầu nối, cho phép Repository cha gọi gián tiếp đến Repository con

@Component
@RequiredArgsConstructor
public class QuizRepositoryAdapter implements QuizRepository {

    private final JpaQuestionRepository jpaQuestionRepository;
    private final JpaQuizRepository jpaQuizRepository;
    private final JpaQuizQuestionRepository jpaQuizQuestionRepository;
    private final JpaOptionRepository jpaOptionRepository;
    private final JpaAttemptRepository jpaAttemptRepository;
    private final JpaResponseRepository jpaResponseRepository;

    @Override
    public List<Question> findQuestionsByChapterId(String chapterId) {
        return jpaQuestionRepository.findByChapterChapterId(chapterId);
    }

    @Override
    public Question save(Question question) {
        return jpaQuestionRepository.save(question);
    }

    @Override
    public Quiz save(Quiz quiz) {
        return jpaQuizRepository.save(quiz);
    }

    @Override
    public QuizQuestion save(QuizQuestion quizQuestion) {
        return jpaQuizQuestionRepository.save(quizQuestion);
    }

    @Override
    public List<QuizQuestion> findQuizQuestionsByQuizId(String quizId) {
        return jpaQuizQuestionRepository.findByQuizQuizIdOrderByIndexOrder(quizId);
    }

    @Override
    public Option save(Option option) {
        return jpaOptionRepository.save(option);
    }

    @Override
    public List<Option> findOptionsByQuestionId(String questionId) {
        return jpaOptionRepository.findByQuestionId(questionId);
    }

    @Override
    public Attempt save(Attempt attempt) {
        return jpaAttemptRepository.save(attempt);
    }

    @Override
    public Response save(Response response) {
        return jpaResponseRepository.save(response);
    }

    @Override
    public List<Response> findResponsesByAttemptId(String attemptId) {
        return jpaResponseRepository.findByAttemptId(attemptId);
    }
}
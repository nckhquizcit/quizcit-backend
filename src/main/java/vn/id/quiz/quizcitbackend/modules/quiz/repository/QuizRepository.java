package vn.id.quiz.quizcitbackend.modules.quiz.repository;

import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Attempt;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Option;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizQuestion;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Response;

import java.util.List;


public interface QuizRepository {

 // Question
    List<Question> findQuestionsByChapterId(String chapterId);

    Question save(Question question);

    // Quiz
    Quiz save(Quiz quiz);

    // QuizQuestion
    QuizQuestion save(QuizQuestion quizQuestion);

    List<QuizQuestion> findQuizQuestionsByQuizId(String quizId);

    // Option
    Option save(Option option);

    List<Option> findOptionsByQuestionId(String questionId);

    // Attempt
    Attempt save(Attempt attempt);

    // Response
    Response save(Response response);

    List<Response> findResponsesByAttemptId(String attemptId);
}
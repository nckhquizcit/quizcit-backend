package vn.id.quiz.quizcitbackend.modules.quiz.repository;

import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizQuestion;

import java.util.List;


public interface QuizRepository {

    List<Question> findQuestionsByChapterId(String chapterId);

    Quiz save(Quiz quiz);

    QuizQuestion save(QuizQuestion quizQuestion);
}
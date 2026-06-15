package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.util.List;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.CreateQuizRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.QuizResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.UpdateQuizRequest;

public interface QuizService {

    QuizResponse createQuiz(
            CreateQuizRequest request);

    void updateQuiz(
            String quizId,
            UpdateQuizRequest request);

    void deleteQuiz(
            String quizId);

    QuizResponse getQuizById(
            String quizId);

    List<QuizResponse> getQuizzesByChapter(
            String chapterId);
}

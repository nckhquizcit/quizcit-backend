package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.util.List;

import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.QuizResultResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.StartQuizResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.SubmitQuizRequest;

public interface AttemptService {

    StartQuizResponse startQuiz(
        String userId,
        String quizId);

    QuizResultResponse submitQuiz(
            SubmitQuizRequest request);

    QuizResultResponse getResult(
            String attemptId);

    List<QuizResultResponse> getUserResults(
            String userId);
}

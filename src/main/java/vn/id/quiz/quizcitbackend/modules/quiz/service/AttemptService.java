package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.util.List;

import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.QuizResultResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.StartQuizResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.SubmitQuizRequest;

public interface AttemptService {
    //Tạo lần thử mới
    StartQuizResponse startQuiz(
        String userId,
        String quizId);
    //Nộp bài
    QuizResultResponse submitQuiz(
            SubmitQuizRequest request);
    //Xem lại bài cũ
    QuizResultResponse getResult(
            String attemptId);
    //Lấy user sở hữu attempt để kiểm tra ownership ở controller
    String getAttemptOwnerId(
            String attemptId);
    //Xem lịch sử làm bài
    List<QuizResultResponse> getUserResults(
            String userId);
}

package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.util.List;
import java.util.Map;

import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.ActiveAttemptResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.AttemptStateResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.QuizResultResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.StartQuizResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.SubmitQuizRequest;

public interface AttemptService {
    //Tạo lần thử mới
    StartQuizResponse startQuiz(
        String userId,
        String quizId);

    AttemptStateResponse getAttemptState(String attemptId);

    List<ActiveAttemptResponse> getActiveAttempts(String userId);

    void saveDraft(String userId, String attemptId, Map<String, String> answers);

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

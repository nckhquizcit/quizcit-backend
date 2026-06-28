package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.util.List;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.CreateQuizRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.QuizResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.UpdateQuizRequest;

public interface QuizService {
    //Tạo đề
    QuizResponse createQuiz(
            CreateQuizRequest request);
    //Sửa đề
    void updateQuiz(
            String quizId,
            UpdateQuizRequest request);
    //Xóa đề
    void deleteQuiz(
            String quizId);
    //Lấy đề
    QuizResponse getQuizById(
            String quizId);
    //Lấy đề theo chương
    List<QuizResponse> getQuizzesByChapter(
            String chapterId);
}

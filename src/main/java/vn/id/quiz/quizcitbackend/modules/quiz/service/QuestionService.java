package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.util.List;

import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.CreateQuestionRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.QuestionResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.UpdateQuestionRequest;

public interface QuestionService {
    //Tạo câu hỏi
    QuestionResponse createQuestion(
            CreateQuestionRequest request);
    //Cập nhật câu hỏi
    void updateQuestion(
            String questionId,
            UpdateQuestionRequest request);
    //Xóa câu hỏi
    void deleteQuestion(
            String questionId);
    //Lấy câu hỏi = id
    QuestionResponse getQuestionById(
            String questionId);
    //Lấy toàn bộ câu hỏi của chương
    List<QuestionResponse> getQuestionsByChapter(
            String chapterId);

    // Phase 2 (P0): Lấy danh sách câu hỏi trong một quiz kèm theo option
    List<QuestionResponse> getQuestionsByQuiz(String quizId);
}

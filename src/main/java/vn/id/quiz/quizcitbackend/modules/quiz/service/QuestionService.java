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

    //Khá chắc là sau này cần lấy toàn bộ câu hỏi thuộc kỹ năng
}
package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.util.List;

import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.AssignQuestionPlacementRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.BulkAssignQuestionPlacementRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.CreateQuestionRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.QuestionResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.UpdateQuestionRequest;

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
            String questionId,
            boolean includeCorrectAnswer);
    //Lấy toàn bộ câu hỏi của chương
    List<QuestionResponse> getQuestionsByChapter(
            String chapterId);
    List<QuestionResponse> getAllQuestions();

    // Phase 2 (P0): Lấy danh sách câu hỏi trong một quiz kèm theo option
    List<QuestionResponse> getQuestionsByQuiz(String quizId);

    // Gắn nhanh môn học và chương cho câu hỏi
    void assignQuestionPlacement(String questionId, AssignQuestionPlacementRequest request);

    // Gắn nhanh môn học và chương cho nhiều câu hỏi
    void bulkAssignQuestionPlacement(BulkAssignQuestionPlacementRequest request);
}

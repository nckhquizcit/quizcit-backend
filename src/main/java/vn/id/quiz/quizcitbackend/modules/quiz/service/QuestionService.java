package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.util.List;

import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.CreateQuestionRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.QuestionResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.UpdateQuestionRequest;

public interface QuestionService {

    QuestionResponse createQuestion(
            CreateQuestionRequest request);

    void updateQuestion(
            String questionId,
            UpdateQuestionRequest request);

    void deleteQuestion(
            String questionId);

    QuestionResponse getQuestionById(
            String questionId);

    List<QuestionResponse> getQuestionsByChapter(
            String chapterId);
}
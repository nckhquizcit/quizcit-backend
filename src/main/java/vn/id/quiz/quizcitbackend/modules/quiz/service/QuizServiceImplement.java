package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.CreateQuizRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.QuizResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.UpdateQuizRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizQuestion;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

@Service
@RequiredArgsConstructor
public class QuizServiceImplement implements QuizService {

    private final QuizRepository quizRepository;
    private final ContentRepository contentRepository;
    private final QuizMapper mapper;

    @Override
    @Transactional
    public QuizResponse createQuiz(
            CreateQuizRequest request) {

        contentRepository.findSubjectById(request.getSubjectId())
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy môn học",
                        HttpStatus.NOT_FOUND));

        contentRepository.findChapterById(request.getChapterId())
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy chương học",
                        HttpStatus.NOT_FOUND));

        List<Question> selected;

        if (request.getQuestionIds() != null && !request.getQuestionIds().isEmpty()) {
            selected = request.getQuestionIds().stream()
                    .map(questionId -> quizRepository.findQuestionById(questionId)
                            .orElseThrow(() -> new AppException(
                                    "Không tìm thấy câu hỏi",
                                    HttpStatus.NOT_FOUND)))
                    .filter(question -> Objects.equals(question.getChapterId(), request.getChapterId()))
                    .toList();

            if (selected.size() != request.getQuestionIds().size()) {
                throw new AppException(
                        "Có câu hỏi không thuộc chương đã chọn",
                        HttpStatus.BAD_REQUEST);
            }
        } else {
            List<Question> questions = quizRepository.findQuestionsByChapterId(
                    request.getChapterId());

            if (questions.isEmpty()) {
                throw new AppException(
                        "Không có câu hỏi",
                        HttpStatus.BAD_REQUEST);
            }

            Collections.shuffle(questions);

            selected = questions.stream()
                    .limit(request.getTotalQuestion())
                    .toList();
        }

        if (selected.isEmpty()) {
            throw new AppException(
                    "Vui lòng chọn ít nhất một câu hỏi",
                    HttpStatus.BAD_REQUEST);
        }

        Quiz quiz = Quiz.create(
                request.getSubjectId(),
                request.getChapterId(),
                request.getName(),
                selected.size(),
                request.getType(),
                request.getDuration());

        quizRepository.save(quiz);

        int order = 1;

        for (Question q : selected) {

            quizRepository.save(
                    QuizQuestion.create(
                            quiz.getQuizId(),
                            q.getQuestionId(),
                            order++));
        }

        return mapper.toResponse(quiz);
    }

    @Override
    @Transactional
    public void updateQuiz(
            String quizId,
            UpdateQuizRequest request) {

        Quiz quiz = quizRepository.findQuizById(
                quizId)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy quiz",
                        HttpStatus.NOT_FOUND));

        Quiz updated = Quiz.builder()
                .quizId(quiz.getQuizId())
                .subjectId(quiz.getSubjectId())
                .chapterId(quiz.getChapterId())
                .name(request.getName())
                .type(quiz.getType())
                .totalQuestion(quiz.getTotalQuestion())
                .duration(request.getDuration())
                .build();

        quizRepository.save(updated);
    }

    @Override
    @Transactional
    public void deleteQuiz(String quizId) {

        quizRepository.deleteQuizById(quizId);
        quizRepository.deleteQuizQuestionsByQuizId(quizId);
    }

    @Override
    public QuizResponse getQuizById(String quizId) {

        return mapper.toResponse(
                quizRepository.findQuizById(quizId)
                        .orElseThrow(() -> new AppException(
                                "Không tìm thấy quiz",
                                HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<QuizResponse> getQuizzesByChapter(
            String chapterId) {

        return mapper.toResponseList(
                quizRepository.findQuizzesByChapterId(chapterId));
    }
}
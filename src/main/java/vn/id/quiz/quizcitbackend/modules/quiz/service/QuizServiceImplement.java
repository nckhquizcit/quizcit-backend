package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.content.service.SubjectVisibilityGuard;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.CreateQuizRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.QuizResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.UpdateQuizRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizQuestion;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizType;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;
import vn.id.quiz.quizcitbackend.shared.dto.ReorderRequest;

@Service
@RequiredArgsConstructor
public class QuizServiceImplement implements QuizService {

    private final QuizRepository quizRepository;
    private final ContentRepository contentRepository;
    private final QuizMapper mapper;
    private final SubjectVisibilityGuard visibilityGuard;

    @Override
    @Transactional
    public QuizResponse createQuiz(
            CreateQuizRequest request) {

        contentRepository.findSubjectById(request.getSubjectId())
                .orElseThrow(() -> new AppException(
                        ErrorMessages.SUBJECT_NOT_FOUND,
                        HttpStatus.NOT_FOUND));

        contentRepository.findChapterById(request.getChapterId())
                .orElseThrow(() -> new AppException(
                        ErrorMessages.CHAPTER_NOT_FOUND,
                        HttpStatus.NOT_FOUND));

        List<Question> selected;

        if (request.getQuestionIds() != null && !request.getQuestionIds().isEmpty()) {
            Map<String, Question> questionsById = quizRepository.findQuestionsByIds(request.getQuestionIds())
                    .stream().collect(java.util.stream.Collectors.toMap(Question::getQuestionId, question -> question));
            selected = request.getQuestionIds().stream()
                    .map(questionId -> java.util.Optional.ofNullable(questionsById.get(questionId))
                            .orElseThrow(() -> new AppException(ErrorMessages.QUESTION_NOT_FOUND, HttpStatus.NOT_FOUND)))
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
                request.getDuration(),
                positiveOrDefault(request.getDisplayOrder(), quizRepository.nextQuizDisplayOrder(request.getChapterId())));

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
                        ErrorMessages.QUIZ_NOT_FOUND,
                        HttpStatus.NOT_FOUND));

        String chapterId = request.getChapterId() == null ? quiz.getChapterId() : request.getChapterId();
        contentRepository.findChapterById(chapterId)
                .filter(chapter -> quiz.getSubjectId().equals(chapter.getSubjectId()))
                .orElseThrow(() -> new AppException(ErrorMessages.CHAPTER_NOT_FOUND, HttpStatus.NOT_FOUND));

        List<Question> selected = null;
        if (request.getQuestionIds() != null) {
            if (request.getQuestionIds().isEmpty()) {
                throw new AppException("Vui lòng chọn ít nhất một câu hỏi", HttpStatus.BAD_REQUEST);
            }
            if (request.getQuestionIds().stream().distinct().count() != request.getQuestionIds().size()) {
                throw new AppException("Danh sách câu hỏi không được trùng lặp", HttpStatus.BAD_REQUEST);
            }
            Map<String, Question> questionsById = quizRepository.findQuestionsByIds(request.getQuestionIds())
                    .stream().collect(java.util.stream.Collectors.toMap(Question::getQuestionId, question -> question));
            selected = request.getQuestionIds().stream()
                    .map(questionId -> java.util.Optional.ofNullable(questionsById.get(questionId))
                            .orElseThrow(() -> new AppException(ErrorMessages.QUESTION_NOT_FOUND, HttpStatus.NOT_FOUND)))
                    .filter(question -> chapterId.equals(question.getChapterId()))
                    .toList();
            if (selected.size() != request.getQuestionIds().size()) {
                throw new AppException("Có câu hỏi không thuộc chương đã chọn", HttpStatus.BAD_REQUEST);
            }
        }

        Quiz updated = Quiz.builder()
                .quizId(quiz.getQuizId())
                .subjectId(quiz.getSubjectId())
                .chapterId(chapterId)
                .name(request.getName())
                .type(quiz.getType())
                .totalQuestion(selected == null ? quiz.getTotalQuestion() : selected.size())
                .duration(request.getDuration())
                .displayOrder(positiveOrDefault(request.getDisplayOrder(), quiz.getDisplayOrder()))
                .createdAt(quiz.getCreatedAt())
                .build();

        quizRepository.save(updated);

        if (selected != null) {
            quizRepository.deleteQuizQuestionsByQuizId(quizId);
            int order = 1;
            for (Question question : selected) {
                quizRepository.save(QuizQuestion.create(quizId, question.getQuestionId(), order++));
            }
        }
    }

    @Override
    @Transactional
    public void deleteQuiz(String quizId) {
        Quiz quiz = quizRepository.findQuizById(quizId)
                .orElseThrow(() -> new AppException(
                        ErrorMessages.QUIZ_NOT_FOUND,
                        HttpStatus.NOT_FOUND));
        quiz.softDelete();
        quizRepository.save(quiz);
    }

    @Override
    @Transactional
    public void hardDeleteQuiz(String quizId) {
        if (quizRepository.findQuizById(quizId).isEmpty()) {
            throw new AppException(
                    ErrorMessages.QUIZ_NOT_FOUND,
                    HttpStatus.NOT_FOUND);
        }
        if (quizRepository.existsAttemptByQuizId(quizId)) {
            throw new AppException(
                    "Đề thi đã có lượt làm bài, không thể xóa vĩnh viễn. Vui lòng sử dụng tính năng xóa mềm (ẩn đề thi).",
                    HttpStatus.BAD_REQUEST);
        }
        quizRepository.hardDeleteQuizById(quizId);
    }

    @Override
    public QuizResponse getQuizById(String quizId) {
        visibilityGuard.requireVisibleQuiz(quizId);
        return mapper.toResponse(
                quizRepository.findQuizById(quizId)
                        .orElseThrow(() -> new AppException(
                                ErrorMessages.QUIZ_NOT_FOUND,
                                HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<QuizResponse> getQuizzesByChapter(
            String chapterId) {
        visibilityGuard.requireVisibleChapter(chapterId);
        return mapper.toResponseList(
                quizRepository.findQuizzesByChapterId(chapterId).stream()
                        .filter(quiz -> quiz.getType() != QuizType.ADAPTIVE)
                        .toList());
    }

    @Override
    @Transactional
    public void reorderQuizzes(String chapterId, ReorderRequest request) {
        contentRepository.findChapterById(chapterId)
                .orElseThrow(() -> new AppException(ErrorMessages.CHAPTER_NOT_FOUND, HttpStatus.NOT_FOUND));
        Map<String, Quiz> quizzesById = quizRepository.findQuizzesByIds(
                request.items().stream().map(item -> item.id()).toList()).stream()
                .collect(java.util.stream.Collectors.toMap(Quiz::getQuizId, quiz -> quiz));
        request.items().forEach(item -> {
            Quiz quiz = java.util.Optional.ofNullable(quizzesById.get(item.id()))
                    .filter(value -> chapterId.equals(value.getChapterId())).orElseThrow(
                            () -> new AppException(ErrorMessages.QUIZ_NOT_FOUND, HttpStatus.NOT_FOUND));
            quiz.updateDisplayOrder(item.displayOrder());
            quizRepository.save(quiz);
        });
    }

    private int positiveOrDefault(Integer requested, int fallback) {
        return requested != null && requested > 0 ? requested : fallback;
    }
}

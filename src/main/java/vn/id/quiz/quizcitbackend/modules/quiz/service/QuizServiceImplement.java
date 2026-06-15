package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.CreateQuizRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.QuizResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.UpdateQuizRequest;
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

        Subject subject = contentRepository.findSubjectById(
                request.getSubjectId())
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy môn học",
                        HttpStatus.NOT_FOUND));

        Chapter chapter = contentRepository.findChapterById(
                request.getChapterId())
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy chương học",
                        HttpStatus.NOT_FOUND));

        List<Question> questions = quizRepository.findQuestionsByChapterId(
                chapter.getChapterId());

        if (questions.isEmpty()) {
            throw new AppException(
                    "Không có câu hỏi",
                    HttpStatus.BAD_REQUEST);
        }

        Collections.shuffle(questions);

        List<Question> selected = questions.stream()
                .limit(request.getTotalQuestion())
                .toList();

        Quiz quiz = Quiz.create(
                subject,
                chapter,
                request.getName(),
                selected.size(),
                request.getType(),
                request.getDuration());

        quizRepository.save(quiz);

        int order = 1;

        for (Question q : selected) {

            quizRepository.save(
                    QuizQuestion.builder()
                            .quiz(quiz)
                            .question(q)
                            .indexOrder(order++)
                            .build());
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
                .subject(quiz.getSubject())
                .chapter(quiz.getChapter())
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
package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.CreateQuestionRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.QuestionResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.UpdateQuestionRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Option;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

@Service
@RequiredArgsConstructor
public class QuestionServiceImplement implements QuestionService {

    private final QuizRepository quizRepository;
    private final ContentRepository contentRepository;
    private final QuestionMapper mapper;

    @Override
    @Transactional
    public QuestionResponse createQuestion(
            CreateQuestionRequest request) {

        contentRepository.findSubjectById(request.getSubjectId())
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy môn học",
                        HttpStatus.NOT_FOUND));

        contentRepository.findChapterById(request.getChapterId())
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy chương học",
                        HttpStatus.NOT_FOUND));

        Question question = Question.create(
                request.getSubjectId(),
                request.getChapterId(),
                request.getContent(),
                request.getExplaination(),
                request.getDifficultyB());

        quizRepository.save(question);

        for (var opt : request.getOptions()) {

            Option option = Option.create(
                    question.getQuestionId(),
                    opt.getContent(),
                    opt.isCorrected());

            quizRepository.save(option);
        }

        return mapper.toResponse(question);
    }

    @Override
    @Transactional
    public void updateQuestion(
            String questionId,
            UpdateQuestionRequest request) {

        Question question = quizRepository.findQuestionById(
                questionId)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy câu hỏi",
                        HttpStatus.NOT_FOUND));

        question = Question.builder()
                .questionId(question.getQuestionId())
                .subjectId(question.getSubjectId())
                .chapterId(question.getChapterId())
                .content(request.getContent())
                .explaination(request.getExplaination())
                .difficultyB(request.getDifficultyB())
                .createdAt(question.getCreatedAt())
                .isActive(question.isActive())
                .build();

        quizRepository.save(question);
    }

    @Override
    @Transactional
    public void deleteQuestion(String questionId) {

        quizRepository.deleteQuestionById(questionId);
    }

    @Override
    public QuestionResponse getQuestionById(String questionId) {

        return mapper.toResponse(
                quizRepository.findQuestionById(questionId)
                        .orElseThrow(() -> new AppException(
                                "Không tìm thấy câu hỏi",
                                HttpStatus.NOT_FOUND)));
    }

    @Override
    public List<QuestionResponse> getQuestionsByChapter(
            String chapterId) {

        return mapper.toResponseList(
                quizRepository.findQuestionsByChapterId(chapterId));
    }
}
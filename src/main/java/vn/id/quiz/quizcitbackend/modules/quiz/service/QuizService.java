package vn.id.quiz.quizcitbackend.modules.quiz.service;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.CreateQuizRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence.QuestionRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence.QuizRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizQuestion;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence.QuizQuestionRepository;
@Service
@RequiredArgsConstructor
public class QuizService {

    private final ContentRepository contentRepository;
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;

    @Transactional
    public Quiz createQuiz(CreateQuizRequest request) {

        Subject subject =
                contentRepository.findSubjectById(request.getSubjectId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("Subject not found"));

        Chapter chapter =
                contentRepository.findChapterById(request.getChapterId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("Chapter not found"));

        List<Question> questions =
                questionRepository.findByChapterChapterId(
                        request.getChapterId());

        if (questions.isEmpty()) {
            throw new IllegalArgumentException(
                    "No questions found for chapter: " + request.getChapterId());
        }

        Collections.shuffle(questions);

        List<Question> selected =
                questions.stream()
                        .limit(request.getTotalQuestion())
                        .toList();

        Quiz quiz = Quiz.create(
                subject,
                chapter,
                request.getName(),
                selected.size(),
                request.getType(),
                request.getDuration()
        );

        quizRepository.save(quiz);

        int order = 1;

        for (Question question : selected) {
            quizQuestionRepository.save(
                    QuizQuestion.builder()
                            .quiz(quiz)
                            .question(question)
                            .indexOrder(order++)
                            .build()
            );
        }

        return quiz;
    }
}
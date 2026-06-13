package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.util.Collections;
import java.util.List;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.CreateQuizRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizQuestion;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final ContentRepository contentRepository;
    private final QuizRepository quizRepository;

    @Transactional
    public Quiz createQuiz(CreateQuizRequest request) {

        Subject subject = contentRepository.findSubjectById(request.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));

        Chapter chapter = contentRepository.findChapterById(request.getChapterId())
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));

        List<Question> questions = quizRepository.findQuestionsByChapterId(
                request.getChapterId());

        if (questions.isEmpty()) {
            throw new IllegalArgumentException(
                    "No questions found for chapter: " + request.getChapterId());
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

        for (Question question : selected) {
            quizRepository.save(
                    QuizQuestion.builder()
                            .quiz(quiz)
                            .question(question)
                            .indexOrder(order++)
                            .build());
        }

        return quiz;
    }
}
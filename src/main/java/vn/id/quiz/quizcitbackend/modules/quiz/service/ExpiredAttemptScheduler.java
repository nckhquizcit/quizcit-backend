package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.SubmitQuizRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Attempt;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;

@Component
@RequiredArgsConstructor
public class ExpiredAttemptScheduler {
    private static final Logger log = LoggerFactory.getLogger(ExpiredAttemptScheduler.class);

    private final QuizRepository quizRepository;
    private final AttemptService attemptService;

    @Scheduled(fixedDelay = 15000)
    public void submitExpiredAttempts() {
        List<Attempt> expired = quizRepository.findExpiredInProgressAttempts(java.time.ZonedDateTime.now());
        for (Attempt attempt : expired) {
            SubmitQuizRequest request = new SubmitQuizRequest();
            request.setAttemptId(attempt.getAttemptId());
            request.setQuizId(attempt.getQuizId());
            request.setAnswers(List.of());
            try {
                attemptService.submitQuiz(request);
            } catch (RuntimeException error) {
                log.warn("Could not auto-submit expired attempt {}", attempt.getAttemptId(), error);
            }
        }
    }
}

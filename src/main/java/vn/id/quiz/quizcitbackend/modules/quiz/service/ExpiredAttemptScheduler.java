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
        if (expired.isEmpty()) {
            return;
        }
        
        org.springframework.security.core.context.SecurityContext context = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            "system", null, List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMINISTRATOR"))
        ));
        org.springframework.security.core.context.SecurityContextHolder.setContext(context);
        
        try {
            for (Attempt attempt : expired) {
                SubmitQuizRequest request = new SubmitQuizRequest();
                request.setAttemptId(attempt.getAttemptId());
                request.setQuizId(attempt.getQuizId());
                request.setAnswers(List.of());
                try {
                    attemptService.submitQuiz(request);
                } catch (vn.id.quiz.quizcitbackend.shared.exceptions.AppException error) {
                    log.warn("Could not auto-submit expired attempt {}: {}", attempt.getAttemptId(), error.getMessage());
                    if (error.getStatus() >= 400 && error.getStatus() < 500 && !"Không tìm thấy attempt".equals(error.getMessage())) {
                        log.info("Marking attempt {} as TIMED_OUT due to unrecoverable error", attempt.getAttemptId());
                        attempt.markTimedOut();
                        quizRepository.save(attempt);
                    }
                } catch (RuntimeException error) {
                    log.warn("Could not auto-submit expired attempt {}: {}", attempt.getAttemptId(), error.getMessage());
                }
            }
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }
}

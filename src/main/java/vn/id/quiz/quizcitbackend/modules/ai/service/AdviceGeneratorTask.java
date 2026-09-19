package vn.id.quiz.quizcitbackend.modules.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.id.quiz.quizcitbackend.modules.ai.dto.SkillDeltaItem;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdviceGeneratorTask {
    
    private final AIService aiService;
    private final QuizRepository quizRepository;

    @Async
    @Transactional
    public void generateAndAttachAdvice(String attemptId, List<SkillDeltaItem> skillDeltas) {
        try {
            String advice = aiService.generateAdvice(skillDeltas);
            quizRepository.findAttemptByIdForUpdate(attemptId).ifPresent(attempt -> {
                attempt.attachAdvice(advice);
                quizRepository.save(attempt);
            });
        } catch (Exception e) {
            log.error("Failed to generate advice for attempt: " + attemptId, e);
        }
    }
}

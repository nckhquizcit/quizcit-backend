package vn.id.quiz.quizcitbackend.modules.quiz.presentation.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;

class AttemptLifecycleTest {

    @Test
    void timedAttemptGetsServerDeadlineAndCanBeAutoSubmitted() {
        ZonedDateTime beforeStart = ZonedDateTime.now();
        Attempt attempt = Attempt.create("user-1", "quiz-1", 3, 30);

        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.IN_PROGRESS);
        assertThat(attempt.getDeadlineAt()).isBetween(
                beforeStart.plusMinutes(29), beforeStart.plusMinutes(31));

        attempt.saveDraftAnswers(Map.of("question-1", "option-1"));
        attempt.complete(1, 0.5);
        attempt.markTimedOut();

        assertThat(attempt.getDraftAnswers()).containsEntry("question-1", "option-1");
        assertThat(attempt.getSubmittedAt()).isNotNull();
        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.TIMED_OUT);
    }

    @Test
    void zeroDurationMeansNoDeadline() {
        Attempt attempt = Attempt.create("user-1", "quiz-1", 3, 0);

        assertThat(attempt.getDeadlineAt()).isNull();
        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.IN_PROGRESS);
    }
}

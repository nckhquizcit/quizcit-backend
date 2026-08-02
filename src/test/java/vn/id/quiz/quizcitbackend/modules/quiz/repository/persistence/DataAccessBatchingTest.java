package vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import vn.id.quiz.quizcitbackend.modules.progress.repository.persistence.JpaQuestionSkillRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;

class DataAccessBatchingTest {

    @Test
    void loadingQuizQuestionsUsesOneBulkQuestionQueryRegardlessOfQuizSize() {
        JpaQuestionRepository questions = mock(JpaQuestionRepository.class);
        JpaQuizQuestionRepository quizQuestions = mock(JpaQuizQuestionRepository.class);
        QuizRepositoryAdapter adapter = new QuizRepositoryAdapter(
                questions, mock(JpaQuizRepository.class), quizQuestions,
                mock(JpaOptionRepository.class), mock(JpaAttemptRepository.class),
                mock(JpaResponseRepository.class), mock(JpaQuestionSkillRepository.class));

        List<Question> rows = IntStream.range(0, 40)
                .mapToObj(index -> Question.builder().questionId("q" + index).build()).toList();
        when(questions.findByQuizIdOrderByQuizIndex("quiz")).thenReturn(rows);

        assertThat(adapter.findQuestionsByQuizId("quiz")).hasSize(40);
        verify(quizQuestions, never()).findByQuizIdOrderByIndexOrder("quiz");
        verify(questions).findByQuizIdOrderByQuizIndex("quiz");
        verify(questions, never()).findByQuestionIdIn(any());
        verify(questions, never()).findByQuestionId(any());
    }
}

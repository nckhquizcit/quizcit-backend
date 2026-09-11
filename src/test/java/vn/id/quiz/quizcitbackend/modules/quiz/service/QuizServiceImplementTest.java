package vn.id.quiz.quizcitbackend.modules.quiz.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.content.service.SubjectVisibilityGuard;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.QuestionResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.QuizResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizType;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;

class QuizServiceImplementTest {

    private QuizRepository quizRepository;
    private ContentRepository contentRepository;
    private QuizMapper mapper;
    private SubjectVisibilityGuard visibilityGuard;
    private QuestionService questionService;
    private QuizServiceImplement quizService;

    @BeforeEach
    void setUp() {
        quizRepository = mock(QuizRepository.class);
        contentRepository = mock(ContentRepository.class);
        mapper = mock(QuizMapper.class);
        visibilityGuard = mock(SubjectVisibilityGuard.class);
        questionService = mock(QuestionService.class);
        quizService = new QuizServiceImplement(
                quizRepository, contentRepository, mapper, visibilityGuard, questionService);
    }

    @Test
    void getSubjectQuizzesOverviewShouldGroupQuizzesByChapter() {
        String subjectId = "sub-1";
        Subject subject = Subject.builder().subjectId(subjectId).courseCode("CS101")
                .name("Computer Science").displayOrder(1).build();
        Chapter ch1 = Chapter.builder().chapterId("ch-1").subjectId(subjectId).name("Chapter 1").displayOrder(1).build();
        Chapter ch2 = Chapter.builder().chapterId("ch-2").subjectId(subjectId).name("Chapter 2").displayOrder(2).build();

        Quiz q1 = Quiz.builder().quizId("quiz-1").chapterId("ch-1").subjectId(subjectId).name("Quiz 1").build();
        QuizResponse qr1 = QuizResponse.builder().quizId("quiz-1").chapterId("ch-1").name("Quiz 1").type(QuizType.CHAPTER).build();

        when(contentRepository.findSubjectById(subjectId)).thenReturn(Optional.of(subject));
        when(contentRepository.findAllChapter(subjectId)).thenReturn(List.of(ch1, ch2));
        when(quizRepository.findNonAdaptiveQuizzesBySubjectId(subjectId)).thenReturn(List.of(q1));
        when(mapper.toResponseList(List.of(q1))).thenReturn(List.of(qr1));

        var overview = quizService.getSubjectQuizzesOverview(subjectId);

        assertThat(overview.subjectId()).isEqualTo(subjectId);
        assertThat(overview.subjectName()).isEqualTo("Computer Science");
        assertThat(overview.chapters()).hasSize(2);

        var chap1 = overview.chapters().get(0);
        assertThat(chap1.chapterId()).isEqualTo("ch-1");
        assertThat(chap1.quizzes()).hasSize(1);
        assertThat(chap1.quizzes().get(0).getQuizId()).isEqualTo("quiz-1");

        var chap2 = overview.chapters().get(1);
        assertThat(chap2.chapterId()).isEqualTo("ch-2");
        assertThat(chap2.quizzes()).isEmpty();
    }

    @Test
    void getQuizDetailWithQuestionsShouldCombineQuizAndQuestions() {
        String quizId = "quiz-1";
        Quiz quiz = Quiz.builder().quizId(quizId).name("Quiz 1").chapterId("ch-1").subjectId("sub-1").build();
        QuizResponse qr = QuizResponse.builder().quizId(quizId).name("Quiz 1").build();
        QuestionResponse question = QuestionResponse.builder().questionId("quest-1").content("Sample question").build();

        when(quizRepository.findQuizById(quizId)).thenReturn(Optional.of(quiz));
        when(mapper.toResponse(quiz)).thenReturn(qr);
        when(questionService.getQuestionsByQuiz(quizId)).thenReturn(List.of(question));

        var result = quizService.getQuizDetailWithQuestions(quizId);

        assertThat(result.quiz().getQuizId()).isEqualTo(quizId);
        assertThat(result.quiz().getName()).isEqualTo("Quiz 1");
        assertThat(result.questions()).hasSize(1);
        assertThat(result.questions().get(0).getQuestionId()).isEqualTo("quest-1");
    }
}

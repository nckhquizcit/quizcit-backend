package vn.id.quiz.quizcitbackend.modules.quiz.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaRequest;
import vn.id.quiz.quizcitbackend.modules.ai.service.AIService;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.UserUnlockedChapter;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaUserUnlockedChapterRepository;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaChapterSkillRepository;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.ChapterSkill;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.AbilityHistory;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.QuestionSkill;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.UserSkillAbility;
import vn.id.quiz.quizcitbackend.modules.progress.repository.ProgressRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.SkillRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.QuestionResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.SubmitAnswerRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Attempt;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Option;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizQuestion;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizType;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Response;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

class AdaptiveQuizServiceTest {

    @Test
    void startPersistsAdaptiveQuizBuffersTenAndReturnsOnlyFive() {
        QuizRepository quizRepository = mock(QuizRepository.class);
        ContentRepository contentRepository = mock(ContentRepository.class);
        ProgressRepository progressRepository = mock(ProgressRepository.class);
        SkillRepository skillRepository = mock(SkillRepository.class);
        JpaUserUnlockedChapterRepository unlockedRepository = mock(JpaUserUnlockedChapterRepository.class);
        JpaChapterSkillRepository chapterSkillRepository = mock(JpaChapterSkillRepository.class);
        QuestionMapper questionMapper = mock(QuestionMapper.class);
        AIService aiService = mock(AIService.class);
        AdaptiveQuizService service = new AdaptiveQuizService(
                quizRepository, contentRepository, progressRepository, skillRepository,
                unlockedRepository, questionMapper, aiService, chapterSkillRepository);

        Chapter chapter = Chapter.builder().chapterId("chapter-1")
                .subjectId("subject-1").name("Chapter").build();
        List<Question> bank = new ArrayList<>();
        List<Option> options = new ArrayList<>();
        List<QuestionSkill> mappings = new ArrayList<>();
        for (int index = 1; index <= 40; index++) {
            String id = "q" + index;
            bank.add(question(id, index / 10.0));
            options.add(option(id, "correct-" + id, true));
            options.add(option(id, "wrong-" + id, false));
            mappings.add(QuestionSkill.create(id, "skill-1", 1.0, true));
        }
        UserSkillAbility ability = UserSkillAbility.create("user-1", "skill-1");
        ability.updateAbility(0.0);
        List<QuizQuestion> assigned = new ArrayList<>();
        AtomicReference<Quiz> savedQuiz = new AtomicReference<>();
        AtomicReference<Attempt> savedAttempt = new AtomicReference<>();

        when(contentRepository.findSubjectById("subject-1")).thenReturn(Optional.of(
                Subject.builder().subjectId("subject-1").name("Subject").visible(true).build()));
        when(contentRepository.findAllChapter("subject-1")).thenReturn(List.of(chapter));
        when(unlockedRepository.findByUserIdAndChapterIdIn(any(), any())).thenReturn(List.of(
                UserUnlockedChapter.create("user-1", "chapter-1")));
        when(chapterSkillRepository.findByChapterId("chapter-1")).thenReturn(List.of(
                ChapterSkill.create("chapter-1", "skill-1", -4.0)));
        when(progressRepository.findUserSkillAbilities("user-1")).thenReturn(List.of(ability));
        when(progressRepository.findQuestionSkillsByQuestionIds(any())).thenAnswer(invocation -> {
            Collection<String> ids = invocation.getArgument(0);
            return mappings.stream().filter(mapping -> ids.contains(mapping.getQuestionId())).toList();
        });
        when(quizRepository.findQuestionsByChapterId("chapter-1")).thenReturn(bank);
        when(quizRepository.findOptionsByQuestionIds(any())).thenAnswer(invocation -> {
            Collection<String> ids = invocation.getArgument(0);
            return options.stream().filter(option -> ids.contains(option.getQuestionId())).toList();
        });
        when(quizRepository.findQuestionsByIds(any())).thenAnswer(invocation -> {
            Collection<String> ids = invocation.getArgument(0);
            return bank.stream().filter(question -> ids.contains(question.getQuestionId())).toList();
        });
        when(quizRepository.save(any(Quiz.class))).thenAnswer(invocation -> {
            Quiz saved = invocation.getArgument(0);
            savedQuiz.set(saved);
            return saved;
        });
        when(quizRepository.save(any(Attempt.class))).thenAnswer(invocation -> {
            Attempt saved = invocation.getArgument(0);
            savedAttempt.set(saved);
            return saved;
        });
        when(quizRepository.save(any(QuizQuestion.class))).thenAnswer(invocation -> {
            QuizQuestion saved = invocation.getArgument(0);
            assigned.add(saved);
            return saved;
        });
        when(quizRepository.findQuizQuestionsByQuizId(any())).thenReturn(assigned);
        when(quizRepository.findResponsesByAttemptId(any())).thenReturn(List.of());
        when(questionMapper.toResponse(any(Question.class))).thenAnswer(invocation -> {
            Question source = invocation.getArgument(0);
            return QuestionResponse.builder().questionId(source.getQuestionId())
                    .content(source.getContent()).difficultyB(source.getDifficultyB()).build();
        });

        var state = service.start("user-1", "subject-1", 40);

        assertThat(savedQuiz.get().getType()).isEqualTo(QuizType.ADAPTIVE);
        assertThat(savedQuiz.get().getTotalQuestion()).isEqualTo(40);
        assertThat(savedAttempt.get().getTotalQuestion()).isEqualTo(40);
        assertThat(assigned).hasSize(10);
        assertThat(state.getQuestions()).hasSize(5);
        assertThat(state.getQuestions()).extracting(QuestionResponse::getQuestionId)
                .containsExactlyElementsOf(assigned.subList(0, 5).stream()
                        .map(QuizQuestion::getQuestionId).toList());
        assertThat(state.getQuestions()).allSatisfy(questionResponse ->
                assertThat(questionResponse.getOptions()).allSatisfy(optionResponse ->
                        assertThat(optionResponse.isCorrected()).isFalse()));
    }

    @Test
    void totalQuestionMustBeAtLeastFortyAndDivisibleByFive() {
        assertThatThrownBy(() -> AdaptiveQuizService.validateTotal(35))
                .isInstanceOf(AppException.class);
        assertThatThrownBy(() -> AdaptiveQuizService.validateTotal(42))
                .isInstanceOf(AppException.class);
        assertThatCode(() -> AdaptiveQuizService.validateTotal(40))
                .doesNotThrowAnyException();
        assertThatCode(() -> AdaptiveQuizService.validateTotal(75))
                .doesNotThrowAnyException();
    }

    @Test
    void blendThetaUsesSeventyPercentNewAndThirtyPercentOld() {
        assertThat(AdaptiveQuizService.blendTheta(0.5, 2.5))
                .isCloseTo(1.9, within(0.000001));
    }

    @Test
    void primaryMatchingSkillDeterminesQuestionDistance() {
        Question question = question("q1", 1.0);
        List<QuestionSkill> mappings = List.of(
                QuestionSkill.create("q1", "secondary", 0.5, false),
                QuestionSkill.create("q1", "primary", 0.5, true));

        double distance = AdaptiveQuizService.matchingThetaDistance(
                question, mappings, Map.of("secondary", 1.0, "primary", 0.25));

        assertThat(distance).isCloseTo(0.75, within(0.000001));
    }

    @Test
    void submitBatchUpdatesOnlyExistingSkillsAndWritesHistoryImmediately() {
        QuizRepository quizRepository = mock(QuizRepository.class);
        ContentRepository contentRepository = mock(ContentRepository.class);
        ProgressRepository progressRepository = mock(ProgressRepository.class);
        SkillRepository skillRepository = mock(SkillRepository.class);
        JpaUserUnlockedChapterRepository unlockedRepository = mock(JpaUserUnlockedChapterRepository.class);
        QuestionMapper questionMapper = mock(QuestionMapper.class);
        AIService aiService = mock(AIService.class);
        JpaChapterSkillRepository chapterSkillRepository = mock(JpaChapterSkillRepository.class);

        AdaptiveQuizService service = new AdaptiveQuizService(
                quizRepository, contentRepository, progressRepository, skillRepository,
                unlockedRepository, questionMapper, aiService, chapterSkillRepository);

        Attempt attempt = Attempt.builder()
                .attemptId("attempt-1").userId("user-1").quizId("quiz-1")
                .totalQuestion(40).build();
        Quiz quiz = Quiz.builder()
                .quizId("quiz-1").subjectId("subject-1").chapterId("chapter-1")
                .name("Adaptive").type(QuizType.ADAPTIVE).totalQuestion(40).build();
        List<Question> questions = new ArrayList<>();
        List<QuizQuestion> assigned = new ArrayList<>();
        List<Option> options = new ArrayList<>();
        List<QuestionSkill> mappings = new ArrayList<>();
        for (int index = 1; index <= 40; index++) {
            String id = "q" + index;
            questions.add(question(id, 0.5));
            if (index <= 10) {
                assigned.add(QuizQuestion.create("quiz-1", id, index));
            }
            options.add(option(id, "correct-" + id, true));
            options.add(option(id, "wrong-" + id, false));
            mappings.add(QuestionSkill.create(id, "owned-skill", 0.8, true));
            mappings.add(QuestionSkill.create(id, "unowned-skill", 0.2, false));
        }

        UserSkillAbility ability = UserSkillAbility.create("user-1", "owned-skill");
        ability.updateAbility(0.5);
        List<Response> persistedResponses = new ArrayList<>();
        List<AbilityHistory> histories = new ArrayList<>();
        Map<String, EstimateThetaRequest> thetaRequests = new LinkedHashMap<>();

        when(quizRepository.findAttemptByIdForUpdate("attempt-1")).thenReturn(Optional.of(attempt));
        when(quizRepository.findQuizById("quiz-1")).thenReturn(Optional.of(quiz));
        when(quizRepository.findResponsesByAttemptId("attempt-1"))
                .thenAnswer(invocation -> List.copyOf(persistedResponses));
        when(quizRepository.findQuizQuestionsByQuizId("quiz-1")).thenReturn(assigned);
        when(quizRepository.findQuestionsByChapterId("chapter-1")).thenReturn(questions);
        when(quizRepository.findQuestionsByIds(any())).thenAnswer(invocation -> {
            Collection<String> ids = invocation.getArgument(0);
            return questions.stream().filter(question -> ids.contains(question.getQuestionId())).toList();
        });
        when(quizRepository.findOptionsByQuestionIds(any())).thenAnswer(invocation -> {
            Collection<String> ids = invocation.getArgument(0);
            return options.stream().filter(option -> ids.contains(option.getQuestionId())).toList();
        });
        when(quizRepository.saveResponses(any())).thenAnswer(invocation -> {
            Collection<Response> saved = invocation.getArgument(0);
            persistedResponses.addAll(saved);
            return List.copyOf(saved);
        });
        when(quizRepository.save(any(QuizQuestion.class))).thenAnswer(invocation -> {
            QuizQuestion saved = invocation.getArgument(0);
            assigned.add(saved);
            return saved;
        });
        when(progressRepository.findQuestionSkillsByQuestionIds(any())).thenAnswer(invocation -> {
            Collection<String> ids = invocation.getArgument(0);
            return mappings.stream().filter(mapping -> ids.contains(mapping.getQuestionId())).toList();
        });
        when(progressRepository.findUserSkillAbilities("user-1")).thenReturn(List.of(ability));
        when(progressRepository.findUserSkillAbility("user-1", "owned-skill"))
                .thenReturn(Optional.of(ability));
        when(aiService.estimateThetaBatch(anyMap())).thenAnswer(invocation -> {
            Map<String, EstimateThetaRequest> request = invocation.getArgument(0);
            thetaRequests.putAll(request);
            return Map.of("owned-skill", 2.5);
        });
        when(progressRepository.save(any(AbilityHistory.class))).thenAnswer(invocation -> {
            AbilityHistory history = invocation.getArgument(0);
            histories.add(history);
            return history;
        });
        when(questionMapper.toResponse(any(Question.class))).thenAnswer(invocation -> {
            Question source = invocation.getArgument(0);
            return QuestionResponse.builder()
                    .questionId(source.getQuestionId())
                    .content(source.getContent())
                    .difficultyB(source.getDifficultyB())
                    .build();
        });
        Chapter chapter = Chapter.builder()
                .chapterId("chapter-1").subjectId("subject-1").name("Chapter").build();
        when(contentRepository.findAllChapter("subject-1")).thenReturn(List.of(chapter));
        when(unlockedRepository.findByUserIdAndChapterIdIn(any(), any()))
                .thenReturn(List.of(UserUnlockedChapter.create("user-1", "chapter-1")));
        when(chapterSkillRepository.findByChapterId("chapter-1")).thenReturn(List.of(
                ChapterSkill.create("chapter-1", "owned-skill", -4.0),
                ChapterSkill.create("chapter-1", "unowned-skill", -4.0)));

        var result = service.submitBatch("user-1", "attempt-1", answersForFirstFive());

        assertThat(result.getAnsweredQuestion()).isEqualTo(5);
        assertThat(result.getQuestions()).extracting(QuestionResponse::getQuestionId)
                .containsExactly("q6", "q7", "q8", "q9", "q10");
        assertThat(assigned).hasSize(15);
        assertThat(assigned.stream().map(QuizQuestion::getQuestionId).distinct().count()).isEqualTo(15);
        assertThat(thetaRequests).containsOnlyKeys("owned-skill");
        assertThat(thetaRequests.get("owned-skill").responses()).containsExactly(1, 1, 1, 1, 1);
        assertThat(ability.getTheta()).isCloseTo(1.9, within(0.000001));
        assertThat(histories).hasSize(1);
        assertThat(histories.getFirst().getTheta()).isCloseTo(1.9, within(0.000001));
        assertThat(histories.getFirst().getDelta()).isCloseTo(1.4, within(0.000001));
        verify(progressRepository, never()).findUserSkillAbility("user-1", "unowned-skill");

        var current = service.submitBatch("user-1", "attempt-1", answers(6, 10));

        assertThat(ability.getTheta()).isCloseTo(2.32, within(0.000001));
        assertThat(histories).hasSize(2);
        assertThat(histories.get(1).getTheta()).isCloseTo(2.32, within(0.000001));
        assertThat(histories.get(1).getDelta()).isCloseTo(0.42, within(0.000001));
        assertThat(assigned).hasSize(20);

        while (!current.isCompleted()) {
            List<SubmitAnswerRequest> nextAnswers = current.getQuestions().stream().map(question -> {
                SubmitAnswerRequest answer = new SubmitAnswerRequest();
                answer.setQuestionId(question.getQuestionId());
                answer.setOptionId("correct-" + question.getQuestionId());
                return answer;
            }).toList();
            current = service.submitBatch("user-1", "attempt-1", nextAnswers);
        }

        double expectedFinalTheta = 2.5 + (0.5 - 2.5) * Math.pow(0.3, 8);
        assertThat(persistedResponses).hasSize(40);
        assertThat(histories).hasSize(8);
        assertThat(ability.getTheta()).isCloseTo(expectedFinalTheta, within(0.000001));
        assertThat(attempt.getRawScore()).isEqualTo(40);
        assertThat(attempt.getOverallTheta()).isCloseTo(expectedFinalTheta, within(0.000001));
        assertThat(current.isCompleted()).isTrue();
        assertThat(current.getQuestions()).isEmpty();

        var repeatedSubmit = service.submitBatch(
                "user-1", "attempt-1", answersForFirstFive());
        assertThat(repeatedSubmit.isCompleted()).isTrue();
        assertThat(persistedResponses).hasSize(40);
        assertThat(histories).hasSize(8);
    }

    private static List<SubmitAnswerRequest> answersForFirstFive() {
        return answers(1, 5);
    }

    private static List<SubmitAnswerRequest> answers(int first, int last) {
        List<SubmitAnswerRequest> answers = new ArrayList<>();
        for (int index = first; index <= last; index++) {
            SubmitAnswerRequest answer = new SubmitAnswerRequest();
            answer.setQuestionId("q" + index);
            answer.setOptionId("correct-q" + index);
            answers.add(answer);
        }
        return answers;
    }

    private static Question question(String id, double difficulty) {
        return Question.builder()
                .questionId(id).subjectId("subject-1").chapterId("chapter-1")
                .content("Question " + id).difficultyB(difficulty).build();
    }

    private static Option option(String questionId, String optionId, boolean correct) {
        return Option.builder().optionId(optionId).questionId(questionId)
                .content(optionId).isCorrected(correct).build();
    }
}

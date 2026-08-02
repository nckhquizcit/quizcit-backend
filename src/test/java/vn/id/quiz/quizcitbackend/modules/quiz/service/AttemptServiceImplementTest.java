package vn.id.quiz.quizcitbackend.modules.quiz.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import vn.id.quiz.quizcitbackend.modules.ai.client.AIClient;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaBatchRequest;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaBatchResponse;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaRequest;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaResponse;
import vn.id.quiz.quizcitbackend.modules.ai.service.AIService;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.AbilityHistory;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.QuestionSkill;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.UserSkillAbility;
import vn.id.quiz.quizcitbackend.modules.progress.repository.ProgressRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.SkillRepository;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.Skill;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.QuizResultResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.SubmitAnswerRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.SubmitQuizRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Attempt;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Option;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizQuestion;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizType;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Response;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;

class AttemptServiceImplementTest {

    @Test
    void historyContainsOnlyCompletedNormalAndAdaptiveAttempts() {
        QuizRepository quizRepository = mock(QuizRepository.class);
        ContentRepository contentRepository = mock(ContentRepository.class);
        AttemptMapper mapper = mock(AttemptMapper.class);
        ProgressRepository progressRepository = mock(ProgressRepository.class);
        SkillRepository skillRepository = mock(SkillRepository.class);
        AIService aiService = mock(AIService.class);
        AttemptServiceImplement service = new AttemptServiceImplement(
                quizRepository, contentRepository, mapper, progressRepository,
                skillRepository, aiService);

        Attempt normalIncomplete = attempt("normal-incomplete", "normal-quiz", 5);
        Attempt normalComplete = attempt("normal-complete", "normal-quiz", 5);
        Attempt adaptivePartial = attempt("adaptive-partial", "adaptive-quiz", 40);
        Attempt adaptiveComplete = attempt("adaptive-complete", "adaptive-quiz", 40);
        when(quizRepository.findAttemptsByUserId("user-1")).thenReturn(List.of(
                normalIncomplete, normalComplete, adaptivePartial, adaptiveComplete));
        when(quizRepository.findQuizById("normal-quiz")).thenReturn(Optional.of(
                Quiz.builder().quizId("normal-quiz").type(QuizType.CHAPTER).build()));
        when(quizRepository.findQuizById("adaptive-quiz")).thenReturn(Optional.of(
                Quiz.builder().quizId("adaptive-quiz").type(QuizType.ADAPTIVE).build()));
        when(quizRepository.findResponsesByAttemptId("normal-incomplete")).thenReturn(List.of());
        when(quizRepository.findResponsesByAttemptId("normal-complete"))
                .thenReturn(List.of(Response.create("normal-complete", "q1", "o1", true)));
        when(quizRepository.findResponsesByAttemptId("adaptive-partial"))
                .thenReturn(responses("adaptive-partial", 5));
        when(quizRepository.findResponsesByAttemptId("adaptive-complete"))
                .thenReturn(responses("adaptive-complete", 40));
        when(quizRepository.findResponsesByAttemptIds(any())).thenReturn(java.util.stream.Stream.of(
                List.of(Response.create("normal-complete", "q1", "o1", true)),
                responses("adaptive-partial", 5), responses("adaptive-complete", 40))
                .flatMap(List::stream).toList());
        when(quizRepository.findQuizzesByIds(any())).thenReturn(List.of(
                Quiz.builder().quizId("normal-quiz").type(QuizType.CHAPTER).build(),
                Quiz.builder().quizId("adaptive-quiz").type(QuizType.ADAPTIVE).build()));
        when(contentRepository.findSubjectsByIds(any())).thenReturn(List.of());
        when(contentRepository.findSubjectById(any())).thenReturn(Optional.of(
                Subject.builder().subjectId("subject-1").name("Subject").build()));

        List<QuizResultResponse> results = service.getUserResults("user-1");

        assertThat(results).extracting(QuizResultResponse::getAttemptId)
                .containsExactly("normal-complete", "adaptive-complete");
    }

    @Test
    void submitQuizShouldBlendExistingAbilityThetaWithNewEstimate() {
        QuizRepository quizRepository = mock(QuizRepository.class);
        ContentRepository contentRepository = mock(ContentRepository.class);
        AttemptMapper mapper = mock(AttemptMapper.class);
        ProgressRepository progressRepository = mock(ProgressRepository.class);
        SkillRepository skillRepository = mock(SkillRepository.class);
        AIService aiService = new AIService(new AIClient(null) {
            @Override
            public EstimateThetaResponse estimateTheta(
                    EstimateThetaRequest request) {

                return new EstimateThetaResponse(2.5, 3.25);
            }

            @Override
            public EstimateThetaBatchResponse estimateThetaBatch(
                    EstimateThetaBatchRequest request) {
                List<EstimateThetaBatchResponse.Item> items = request.items().stream()
                        .map(item -> new EstimateThetaBatchResponse.Item(item.key(), 2.5, 3.25))
                        .toList();
                return new EstimateThetaBatchResponse(items);
            }
        });
        AttemptServiceImplement service = new AttemptServiceImplement(
                quizRepository,
                contentRepository,
                mapper,
                progressRepository,
                skillRepository,
                aiService);

        Attempt attempt = Attempt.builder()
                .attemptId("attempt-1")
                .userId("user-1")
                .quizId("quiz-1")
                .totalQuestion(1)
                .build();
        Question question = Question.builder()
                .questionId("question-1")
                .subjectId("subject-1")
                .chapterId("chapter-1")
                .content("Question")
                .difficultyB(0.5)
                .build();
        Option option = Option.builder()
                .optionId("option-1")
                .questionId("question-1")
                .content("Answer")
                .isCorrected(true)
                .build();
        Quiz quiz = Quiz.builder()
                .quizId("quiz-1")
                .subjectId("subject-1")
                .chapterId("chapter-1")
                .name("Quiz visible")
                .build();
        Subject subject = Subject.builder()
                .subjectId("subject-1")
                .courseCode("CT101")
                .name("Lập trình căn bản")
                .visible(true)
                .build();
        UserSkillAbility ability = UserSkillAbility.create("user-1", "skill-1");
        ability.updateAbility(0.5);
        List<AbilityHistory> savedHistories = new ArrayList<>();

        when(quizRepository.findAttemptByIdForUpdate("attempt-1"))
                .thenReturn(Optional.of(attempt));
        when(quizRepository.findAttemptById("attempt-1"))
                .thenReturn(Optional.of(attempt));
        when(quizRepository.findQuizById("quiz-1"))
                .thenReturn(Optional.of(quiz));
        when(contentRepository.findSubjectById("subject-1"))
                .thenReturn(Optional.of(subject));
        when(quizRepository.findResponsesByAttemptId("attempt-1"))
                .thenReturn(List.of());
        when(quizRepository.findQuizQuestionsByQuizId("quiz-1"))
                .thenReturn(List.of(QuizQuestion.builder()
                        .quizId("quiz-1")
                        .questionId("question-1")
                        .indexOrder(1)
                        .build()));
        when(quizRepository.findQuestionsByIds(any()))
                .thenReturn(List.of(question));
        when(quizRepository.findOptionsByQuestionIds(any()))
                .thenReturn(List.of(option));
        when(quizRepository.saveResponses(any()))
                .thenAnswer(invocation -> new ArrayList<>(invocation.getArgument(0)));
        when(progressRepository.findQuestionSkillsByQuestionIds(any()))
                .thenReturn(List.of(QuestionSkill.create(
                        "question-1",
                        "skill-1",
                        1.0,
                        true)));
        when(progressRepository.findUserSkillAbility("user-1", "skill-1"))
                .thenReturn(Optional.of(ability));
        when(progressRepository.findUserSkillAbilities("user-1")).thenReturn(List.of(ability));
        when(progressRepository.saveUserSkillAbilities(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(progressRepository.saveAbilityHistories(any())).thenAnswer(invocation -> {
            List<AbilityHistory> histories = new ArrayList<>(invocation.getArgument(0));
            savedHistories.addAll(histories);
            return histories;
        });
        when(progressRepository.save(any(AbilityHistory.class)))
                .thenAnswer(invocation -> {
                    AbilityHistory history = invocation.getArgument(0);
                    savedHistories.add(history);
                    return history;
                });
        when(progressRepository.findAbilityHistoriesByAttemptId("attempt-1"))
                .thenAnswer(invocation -> savedHistories);

        QuizResultResponse result = service.submitQuiz(submitRequest());

        double blendedTheta = 2.5 * 0.7 + 0.5 * 0.3;
        assertThat(ability.getTheta()).isCloseTo(blendedTheta, within(0.0001));
        assertThat(savedHistories).hasSize(1);
        assertThat(savedHistories.get(0).getTheta())
                .isCloseTo(blendedTheta, within(0.0001));
        assertThat(savedHistories.get(0).getDelta())
                .isCloseTo(blendedTheta - 0.5, within(0.0001));
        assertThat(attempt.getOverallTheta())
                .isCloseTo(blendedTheta, within(0.0001));
        assertThat(result.getSkillResults()).hasSize(1);
        assertThat(result.getSkillResults().get(0).getTheta())
                .isCloseTo(blendedTheta, within(0.0001));
    }

    @Test
    void getResultShouldIncludeMetadataAndQuestionAnswers() {
        QuizRepository quizRepository = mock(QuizRepository.class);
        ContentRepository contentRepository = mock(ContentRepository.class);
        AttemptMapper mapper = mock(AttemptMapper.class);
        ProgressRepository progressRepository = mock(ProgressRepository.class);
        SkillRepository skillRepository = mock(SkillRepository.class);
        AIService aiService = mock(AIService.class);
        AttemptServiceImplement service = new AttemptServiceImplement(
                quizRepository,
                contentRepository,
                mapper,
                progressRepository,
                skillRepository,
                aiService);

        Attempt attempt = Attempt.builder()
                .attemptId("attempt-1")
                .userId("user-1")
                .quizId("quiz-1")
                .rawScore(1)
                .totalQuestion(2)
                .overallTheta(0.75)
                .createdAt(java.time.ZonedDateTime.parse("2026-06-29T10:15:30+07:00"))
                .build();
        Quiz quiz = Quiz.builder()
                .quizId("quiz-1")
                .subjectId("subject-1")
                .chapterId("chapter-1")
                .name("Đề test 5 câu")
                .totalQuestion(2)
                .type(QuizType.CHAPTER)
                .duration(45)
                .build();
        Subject subject = Subject.builder()
                .subjectId("subject-1")
                .courseCode("CT101")
                .name("Lập trình căn bản")
                .build();
        Question questionOne = Question.builder()
                .questionId("question-1")
                .subjectId("subject-1")
                .chapterId("chapter-1")
                .content("Đâu là đáp án C")
                .contentFormat("blocknote")
                .explanation("Vì C đúng.")
                .difficultyB(0.1)
                .build();
        Question questionTwo = Question.builder()
                .questionId("question-2")
                .subjectId("subject-1")
                .chapterId("chapter-1")
                .content("Messi hay Ronaldo?")
                .explanation("Tùy tiêu chí.")
                .difficultyB(0.2)
                .build();
        Option optionOne = Option.builder()
                .optionId("option-1")
                .questionId("question-1")
                .content("C")
                .isCorrected(true)
                .build();
        Option optionTwo = Option.builder()
                .optionId("option-2")
                .questionId("question-1")
                .content("A")
                .isCorrected(false)
                .build();
        Option optionThree = Option.builder()
                .optionId("option-3")
                .questionId("question-2")
                .content("Ronaldo")
                .isCorrected(true)
                .build();
        Option optionFour = Option.builder()
                .optionId("option-4")
                .questionId("question-2")
                .content("Messi")
                .isCorrected(false)
                .build();

        when(quizRepository.findAttemptById("attempt-1"))
                .thenReturn(Optional.of(attempt));
        when(quizRepository.findQuizById("quiz-1"))
                .thenReturn(Optional.of(quiz));
        when(contentRepository.findSubjectById("subject-1"))
                .thenReturn(Optional.of(subject));
        when(quizRepository.findQuizQuestionsByQuizId("quiz-1"))
                .thenReturn(List.of(
                        QuizQuestion.builder()
                                .quizId("quiz-1")
                                .questionId("question-1")
                                .indexOrder(1)
                                .build(),
                        QuizQuestion.builder()
                                .quizId("quiz-1")
                                .questionId("question-2")
                                .indexOrder(2)
                                .build()));
        when(quizRepository.findQuestionsByIds(any()))
                .thenReturn(List.of(questionOne, questionTwo));
        when(quizRepository.findOptionsByQuestionIds(any()))
                .thenReturn(List.of(optionTwo, optionOne, optionThree, optionFour));
        when(quizRepository.findResponsesByAttemptId("attempt-1"))
                .thenReturn(List.of(
                        Response.create("attempt-1", "question-1", "option-1", true),
                        Response.create("attempt-1", "question-2", "option-4", false)));
        when(progressRepository.findAbilityHistoriesByAttemptId("attempt-1"))
                .thenReturn(List.of(
                        AbilityHistory.builder()
                                .historyId("history-old")
                                .userId("user-1")
                                .skillId("skill-1")
                                .theta(0.25)
                                .delta(0.25)
                                .attemptId("attempt-1")
                                .createdAt(java.time.LocalDateTime.parse("2026-06-29T09:00:00"))
                                .build(),
                        AbilityHistory.builder()
                                .historyId("history-new")
                                .userId("user-1")
                                .skillId("skill-1")
                                .theta(0.75)
                                .delta(0.50)
                                .attemptId("attempt-1")
                                .createdAt(java.time.LocalDateTime.parse("2026-06-29T10:00:00"))
                                .build()));
        when(skillRepository.findById("skill-1"))
                .thenReturn(Optional.of(Skill.builder()
                        .skillId("skill-1")
                        .name("Biến và kiểu dữ liệu")
                        .build()));
        when(skillRepository.findByIds(any())).thenReturn(List.of(Skill.builder()
                .skillId("skill-1").name("Biến và kiểu dữ liệu").build()));

        QuizResultResponse result = service.getResult("attempt-1");

        assertThat(result.getQuizName()).isEqualTo("Đề test 5 câu");
        assertThat(result.getSubjectName()).isEqualTo("Lập trình căn bản");
        assertThat(result.getSubmittedAt()).isEqualTo(attempt.getCreatedAt());
        assertThat(result.getSkillResults()).hasSize(1);
        assertThat(result.getSkillResults().get(0).getSkillName())
                .isEqualTo("Biến và kiểu dữ liệu");
        assertThat(result.getSkillResults().get(0).getTheta()).isEqualTo(0.75);
        assertThat(result.getQuestionResults()).hasSize(2);
        assertThat(result.getQuestionResults().get(0).getQuestionNumber()).isEqualTo(1);
        assertThat(result.getQuestionResults().get(0).getContent()).isEqualTo("Đâu là đáp án C");
        assertThat(result.getQuestionResults().get(0).getContentFormat()).isEqualTo("blocknote");
        assertThat(result.getQuestionResults().get(0).getSelectedOptionId()).isEqualTo("option-1");
        assertThat(result.getQuestionResults().get(0).getCorrectOptionId()).isEqualTo("option-1");
        assertThat(result.getQuestionResults().get(0).isCorrect()).isTrue();
        assertThat(result.getQuestionResults().get(0).getOptions()).hasSize(2);
        assertThat(result.getQuestionResults().get(0).getOptions()).allSatisfy(option ->
                assertThat(option.getContentFormat()).isEqualTo("blocknote"));
        assertThat(result.getQuestionResults().get(1).getQuestionNumber()).isEqualTo(2);
        assertThat(result.getQuestionResults().get(1).getSelectedOptionId()).isEqualTo("option-4");
        assertThat(result.getQuestionResults().get(1).getCorrectOptionId()).isEqualTo("option-3");
        assertThat(result.getQuestionResults().get(1).isCorrect()).isFalse();
    }

    private SubmitQuizRequest submitRequest() {
        SubmitAnswerRequest answer = new SubmitAnswerRequest();
        answer.setQuestionId("question-1");
        answer.setOptionId("option-1");

        SubmitQuizRequest request = new SubmitQuizRequest();
        request.setAttemptId("attempt-1");
        request.setQuizId("quiz-1");
        request.setAnswers(List.of(answer));
        return request;
    }

    private static Attempt attempt(String attemptId, String quizId, int total) {
        return Attempt.builder().attemptId(attemptId).userId("user-1")
                .quizId(quizId).totalQuestion(total).build();
    }

    private static List<Response> responses(String attemptId, int count) {
        List<Response> responses = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            responses.add(Response.create(attemptId, "q" + index, "o" + index, true));
        }
        return responses;
    }
}

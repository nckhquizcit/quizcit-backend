package vn.id.quiz.quizcitbackend.modules.quiz.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Theory;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaChapterSkillRepository;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.ChapterSkill;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.QuestionSkill;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.Skill;
import vn.id.quiz.quizcitbackend.modules.progress.repository.SkillRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.option.OptionResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.option.CreateOptionRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.AssignQuestionPlacementRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.BulkAssignQuestionPlacementRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.CreateQuestionRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.QuestionResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.QuestionSkillMappingRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.UpdateQuestionRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Attempt;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Option;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizQuestion;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Response;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
import vn.id.quiz.quizcitbackend.shared.security.CustomUserPrincipal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuestionServiceImplementTest {
    private FakeQuizRepository quizRepository;
    private FakeContentRepository contentRepository;
    private FakeSkillRepository skillRepository;
    private QuestionServiceImplement service;

    @BeforeEach
    void setUp() {
        quizRepository = new FakeQuizRepository();
        contentRepository = new FakeContentRepository();
        skillRepository = new FakeSkillRepository();
        JpaChapterSkillRepository chapterSkillRepository = mock(JpaChapterSkillRepository.class);
        when(chapterSkillRepository.findByChapterId(org.mockito.ArgumentMatchers.anyString())).thenReturn(List.of(
                ChapterSkill.create("chapter-new", "skill-a", 0),
                ChapterSkill.create("chapter-new", "skill-b", 0)));
        service = new QuestionServiceImplement(
                quizRepository,
                contentRepository,
                new FakeQuestionMapper(),
                skillRepository,
                chapterSkillRepository);

        quizRepository.question = Question.create(
                "subject-old",
                "chapter-old",
                "Old content",
                "Old explanation",
                0.0);
        contentRepository.subjects.put(
                "subject-new",
                Subject.builder().subjectId("subject-new").courseCode("NEW").name("New subject").build());
        contentRepository.chapters.put(
                "chapter-new",
                Chapter.builder().chapterId("chapter-new").subjectId("subject-new").name("New chapter").build());
        skillRepository.skills.put("skill-a", Skill.builder().skillId("skill-a").name("Skill A").build());
        skillRepository.skills.put("skill-b", Skill.builder().skillId("skill-b").name("Skill B").build());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void updateQuestionShouldPersistSubjectAndChapterWhenChapterBelongsToSubject() {
        UpdateQuestionRequest request = updateRequest("subject-new", "chapter-new", List.of());

        service.updateQuestion(quizRepository.question.getQuestionId(), request);

        assertThat(quizRepository.savedQuestion.getSubjectId()).isEqualTo("subject-new");
        assertThat(quizRepository.savedQuestion.getChapterId()).isEqualTo("chapter-new");
        assertThat(quizRepository.savedQuestion.getContent()).isEqualTo("Updated content");
    }

    @Test
    void updateOrphanedQuestionShouldFailWhenSubjectOrChapterNotProvided() {
        Question orphaned = Question.builder()
                .questionId("q-orphan")
                .subjectId(null)
                .chapterId(null)
                .content("Orphan content")
                .explanation("Orphan exp")
                .build();
        quizRepository.question = orphaned;

        UpdateQuestionRequest request = updateRequest(null, null, List.of());

        assertThatThrownBy(() -> service.updateQuestion("q-orphan", request))
                .isInstanceOf(AppException.class)
                .hasMessage("Vui lòng chọn môn học và chương cho câu hỏi");
    }

    @Test
    void updateOrphanedQuestionShouldSucceedWhenNewSubjectAndChapterProvided() {
        Question orphaned = Question.builder()
                .questionId("q-orphan")
                .subjectId(null)
                .chapterId(null)
                .content("Orphan content")
                .explanation("Orphan exp")
                .build();
        quizRepository.question = orphaned;

        UpdateQuestionRequest request = updateRequest("subject-new", "chapter-new", List.of());

        service.updateQuestion("q-orphan", request);

        assertThat(quizRepository.savedQuestion.getSubjectId()).isEqualTo("subject-new");
        assertThat(quizRepository.savedQuestion.getChapterId()).isEqualTo("chapter-new");
    }

    @Test
    void getQuestionByIdShouldAllowAdminForOrphanedQuestion() {
        Question orphaned = Question.builder()
                .questionId("q-orphan")
                .subjectId(null)
                .chapterId(null)
                .content("Orphan content")
                .explanation("Orphan exp")
                .build();
        quizRepository.question = orphaned;

        var principal = new CustomUserPrincipal("admin-1", "admin", "admin@example.com", "ADMINISTRATOR", "ACTIVE");
        var auth = new TestingAuthenticationToken(principal, null, List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRATOR")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        QuestionResponse response = service.getQuestionById("q-orphan", true);

        assertThat(response).isNotNull();
        assertThat(response.getQuestionId()).isEqualTo("q-orphan");
        assertThat(response.getSubjectId()).isNull();
        assertThat(response.getChapterId()).isNull();
    }

    @Test
    void getQuestionByIdShouldThrowNotFoundForNonAdminWhenQuestionIsOrphaned() {
        Question orphaned = Question.builder()
                .questionId("q-orphan")
                .subjectId(null)
                .chapterId(null)
                .content("Orphan content")
                .explanation("Orphan exp")
                .build();
        quizRepository.question = orphaned;

        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> service.getQuestionById("q-orphan", false))
                .isInstanceOf(AppException.class)
                .hasMessage("Không tìm thấy câu hỏi");
    }

    @Test
    void assignQuestionPlacementShouldSucceedWhenValidSubjectAndChapter() {
        Question orphaned = Question.builder()
                .questionId("q-orphan")
                .subjectId(null)
                .chapterId(null)
                .content("Orphan content")
                .build();
        quizRepository.question = orphaned;

        AssignQuestionPlacementRequest request = AssignQuestionPlacementRequest.builder()
                .subjectId("subject-new")
                .chapterId("chapter-new")
                .build();

        service.assignQuestionPlacement("q-orphan", request);

        assertThat(quizRepository.savedQuestion.getSubjectId()).isEqualTo("subject-new");
        assertThat(quizRepository.savedQuestion.getChapterId()).isEqualTo("chapter-new");
    }

    @Test
    void assignQuestionPlacementShouldFailWhenChapterMismatch() {
        contentRepository.chapters.put(
                "chapter-other",
                Chapter.builder().chapterId("chapter-other").subjectId("subject-other").name("Other").build());

        AssignQuestionPlacementRequest request = AssignQuestionPlacementRequest.builder()
                .subjectId("subject-new")
                .chapterId("chapter-other")
                .build();

        assertThatThrownBy(() -> service.assignQuestionPlacement(quizRepository.question.getQuestionId(), request))
                .isInstanceOf(AppException.class)
                .hasMessage("Chương học không thuộc môn học đã chọn");
    }

    @Test
    void bulkAssignQuestionPlacementShouldSucceedForMultipleQuestions() {
        Question q1 = Question.builder().questionId("q-1").build();
        Question q2 = Question.builder().questionId("q-2").build();
        quizRepository.questionsById.put("q-1", q1);
        quizRepository.questionsById.put("q-2", q2);

        BulkAssignQuestionPlacementRequest request = BulkAssignQuestionPlacementRequest.builder()
                .questionIds(List.of("q-1", "q-2"))
                .subjectId("subject-new")
                .chapterId("chapter-new")
                .build();

        service.bulkAssignQuestionPlacement(request);

        assertThat(q1.getSubjectId()).isEqualTo("subject-new");
        assertThat(q1.getChapterId()).isEqualTo("chapter-new");
        assertThat(q2.getSubjectId()).isEqualTo("subject-new");
        assertThat(q2.getChapterId()).isEqualTo("chapter-new");
    }

    @Test
    void updateQuestionShouldRejectChapterThatDoesNotBelongToSubject() {
        contentRepository.chapters.put(
                "chapter-other",
                Chapter.builder().chapterId("chapter-other").subjectId("subject-other").name("Other").build());

        UpdateQuestionRequest request = updateRequest("subject-new", "chapter-other", List.of());

        assertThatThrownBy(() -> service.updateQuestion(quizRepository.question.getQuestionId(), request))
                .isInstanceOf(AppException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(quizRepository.savedQuestion).isNull();
    }

    @Test
    void updateQuestionShouldRejectDuplicateSkills() {
        UpdateQuestionRequest request = updateRequest(
                "subject-new",
                "chapter-new",
                List.of(skill("skill-a", 0.5, true), skill("skill-a", 0.5, false)));

        assertThatThrownBy(() -> service.updateQuestion(quizRepository.question.getQuestionId(), request))
                .isInstanceOf(AppException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void updateQuestionShouldRejectNegativeSkillWeight() {
        UpdateQuestionRequest request = updateRequest(
                "subject-new",
                "chapter-new",
                List.of(skill("skill-a", -0.1, true), skill("skill-b", 1.1, false)));

        assertThatThrownBy(() -> service.updateQuestion(quizRepository.question.getQuestionId(), request))
                .isInstanceOf(AppException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void updateQuestionShouldRejectSkillWeightSumDifferentFromOne() {
        UpdateQuestionRequest request = updateRequest(
                "subject-new",
                "chapter-new",
                List.of(skill("skill-a", 0.4, true), skill("skill-b", 0.4, false)));

        assertThatThrownBy(() -> service.updateQuestion(quizRepository.question.getQuestionId(), request))
                .isInstanceOf(AppException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void updateQuestionShouldRejectMissingPrimarySkill() {
        UpdateQuestionRequest request = updateRequest(
                "subject-new",
                "chapter-new",
                List.of(skill("skill-a", 0.5, false), skill("skill-b", 0.5, false)));

        assertThatThrownBy(() -> service.updateQuestion(quizRepository.question.getQuestionId(), request))
                .isInstanceOf(AppException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void createQuestionShouldRejectMultipleCorrectAnswers() {
        CreateQuestionRequest request = createRequest(true, true);

        assertThatThrownBy(() -> service.createQuestion(request))
                .isInstanceOf(AppException.class)
                .hasMessage("Câu hỏi phải có đúng một đáp án đúng");
    }

    @Test
    void getQuestionsWithSubjectAndChapterShouldReturnFilteredQuestions() {
        Question q = Question.builder()
                .questionId("q-filter")
                .subjectId("subject-new")
                .chapterId("chapter-new")
                .content("Filtered question")
                .build();
        quizRepository.filteredQuestions = List.of(q);

        var results = service.getQuestions("subject-new", "chapter-new");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getQuestionId()).isEqualTo("q-filter");
    }

    @Test
    void getQuestionsPagedShouldReturnPagedResults() {
        Question q = Question.builder()
                .questionId("q-filter-page")
                .subjectId("subject-new")
                .chapterId("chapter-new")
                .content("Paged question")
                .build();
        quizRepository.filteredQuestions = List.of(q);

        var page = service.getQuestionsPaged("subject-new", "chapter-new", org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getQuestionId()).isEqualTo("q-filter-page");
    }

    @Test
    void createQuestionShouldRejectFewerThanTwoAnswers() {
        CreateQuestionRequest request = createRequest(true);

        assertThatThrownBy(() -> service.createQuestion(request))
                .isInstanceOf(AppException.class)
                .hasMessage("Câu hỏi phải có ít nhất hai đáp án");
    }

    private static CreateQuestionRequest createRequest(boolean... correctAnswers) {
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setSubjectId("subject-new");
        request.setChapterId("chapter-new");
        request.setContent("Question");
        List<CreateOptionRequest> options = new ArrayList<>();
        for (int index = 0; index < correctAnswers.length; index++) {
            CreateOptionRequest option = new CreateOptionRequest();
            option.setContent("Option " + index);
            option.setCorrected(correctAnswers[index]);
            options.add(option);
        }
        request.setOptions(options);
        return request;
    }

    private static UpdateQuestionRequest updateRequest(
            String subjectId,
            String chapterId,
            List<QuestionSkillMappingRequest> skills) {
        UpdateQuestionRequest request = new UpdateQuestionRequest();
        request.setSubjectId(subjectId);
        request.setChapterId(chapterId);
        request.setContent("Updated content");
        request.setExplanation("Updated explanation");
        request.setDifficultyB(0.0);
        request.setSkills(skills);
        return request;
    }

    private static QuestionSkillMappingRequest skill(String skillId, double weight, boolean primary) {
        QuestionSkillMappingRequest skill = new QuestionSkillMappingRequest();
        skill.setSkillId(skillId);
        skill.setWeight(weight);
        skill.setPrimary(primary);
        return skill;
    }

    private static class FakeQuizRepository implements QuizRepository {
        private Question question;
        private Question savedQuestion;
        private final Map<String, Question> questionsById = new HashMap<>();
        private final List<QuestionSkill> savedQuestionSkills = new ArrayList<>();
        private List<Question> filteredQuestions = new ArrayList<>();


        @Override
        public boolean existsQuestionByChapterIdAndContent(String chapterId, String content) {
            return false;
        }

        @Override
        public List<Question> findQuestionsBySubjectIdAndChapterId(String subjectId, String chapterId) {
            return filteredQuestions;
        }

        @Override
        public org.springframework.data.domain.Page<Question> findQuestionsPaged(
                String subjectId, String chapterId, org.springframework.data.domain.Pageable pageable) {
            return new org.springframework.data.domain.PageImpl<>(filteredQuestions, pageable, filteredQuestions.size());
        }

        @Override
        public Question save(Question question) {
            this.savedQuestion = question;
            return question;
        }

        @Override
        public Optional<Question> findQuestionById(String questionId) {
            if (questionsById.containsKey(questionId)) {
                return Optional.ofNullable(questionsById.get(questionId));
            }
            return Optional.ofNullable(question);
        }

        @Override
        public List<Question> findQuestionsByChapterId(String chapterId) {
            return List.of();
        }

        @Override
        public List<Question> findQuestionsByIds(java.util.Collection<String> questionIds) {
            if (questionIds == null || questionIds.isEmpty()) return List.of();
            return questionIds.stream().map(questionsById::get).filter(java.util.Objects::nonNull).toList();
        }

        @Override
        public void deleteQuestionById(String questionId) {
        }

        @Override
        public Quiz save(Quiz quiz) {
            return quiz;
        }

        @Override
        public Optional<Quiz> findQuizById(String quizId) {
            return Optional.empty();
        }

        @Override
        public List<Quiz> findQuizzesByChapterId(String chapterId) {
            return List.of();
        }

        @Override
        public List<Quiz> findQuizzesBySubjectId(String subjectId) {
            return List.of();
        }

        @Override
        public void deleteQuizById(String quizId) {
        }

        @Override
        public QuizQuestion save(QuizQuestion quizQuestion) {
            return quizQuestion;
        }

        @Override
        public List<QuizQuestion> findQuizQuestionsByQuizId(String quizId) {
            return List.of();
        }

        @Override
        public void deleteQuizQuestionsByQuestionId(String questionId) {
        }

        @Override
        public void deleteQuizQuestionsByQuizId(String quizId) {
        }

        @Override
        public Option save(Option option) {
            return option;
        }

        @Override
        public Optional<Option> findOptionById(String optionId) {
            return Optional.empty();
        }

        @Override
        public List<Option> findOptionsByQuestionId(String questionId) {
            return List.of();
        }

        @Override
        public List<Option> findOptionsByQuestionIds(java.util.Collection<String> questionIds) {
            return List.of();
        }

        @Override
        public void deleteOptionsByQuestionId(String questionId) {
        }

        @Override
        public Attempt save(Attempt attempt) {
            return attempt;
        }

        @Override
        public Optional<Attempt> findAttemptById(String attemptId) {
            return Optional.empty();
        }

        @Override
        public List<Attempt> findAttemptsByUserId(String userId) {
            return List.of();
        }

        @Override
        public Response save(Response response) {
            return response;
        }

        @Override
        public List<Response> saveResponses(java.util.Collection<Response> responses) {
            return new ArrayList<>(responses);
        }

        @Override
        public List<Response> findResponsesByAttemptId(String attemptId) {
            return List.of();
        }

        @Override
        public void deleteResponsesByQuestionId(String questionId) {
        }

        @Override
        public List<Question> findQuestionsByQuizId(String quizId) {
            return List.of();
        }

        @Override
        public QuestionSkill saveQuestionSkill(QuestionSkill questionSkill) {
            savedQuestionSkills.add(questionSkill);
            return questionSkill;
        }

        @Override
        public List<QuestionSkill> findQuestionSkillsByQuestionId(String questionId) {
            return List.of();
        }

        @Override
        public void deleteQuestionSkillsByQuestionId(String questionId) {
            savedQuestionSkills.clear();
        }
    }

    private static class FakeContentRepository implements ContentRepository {
        private final Map<String, Subject> subjects = new HashMap<>();
        private final Map<String, Chapter> chapters = new HashMap<>();

        @Override
        public List<Subject> findAllSubject() {
            return List.copyOf(subjects.values());
        }

        @Override
        public List<Chapter> findAllChapter(String subjectId) {
            return chapters.values().stream()
                    .filter(chapter -> subjectId.equals(chapter.getSubjectId()))
                    .toList();
        }

        @Override
        public List<Theory> findAllTheory(String chapterId) {
            return List.of();
        }

        @Override
        public Subject save(Subject subject) {
            return subject;
        }

        @Override
        public Chapter save(Chapter chapter) {
            return chapter;
        }

        @Override
        public Theory save(Theory theory) {
            return theory;
        }

        @Override
        public Optional<Subject> findSubjectById(String id) {
            return Optional.ofNullable(subjects.get(id));
        }

        @Override
        public Optional<Theory> findTheoryById(String id) {
            return Optional.empty();
        }

        @Override
        public Optional<Chapter> findChapterById(String id) {
            return Optional.ofNullable(chapters.get(id));
        }

        @Override
        public boolean existsByCourseCode(String courseCode) {
            return false;
        }

        @Override
        public boolean existsBySubjectId(String subjectId) {
            return subjects.containsKey(subjectId);
        }

        @Override
        public boolean existsByChapterId(String chapterId) {
            return chapters.containsKey(chapterId);
        }

        @Override
        public boolean existsByTheoryId(String theoryId) {
            return false;
        }

        @Override
        public void deleteSubjectById(String subjectId) {
        }

        @Override
        public void deleteChapterById(String chapterId) {
        }

        @Override
        public void deleteTheoryById(String theoryId) {
        }
    }

    private static class FakeSkillRepository implements SkillRepository {
        private final Map<String, Skill> skills = new HashMap<>();

        @Override
        public List<Skill> findAll() {
            return List.copyOf(skills.values());
        }

        @Override
        public Optional<Skill> findById(String skillId) {
            return Optional.ofNullable(skills.get(skillId));
        }

        @Override
        public Skill save(Skill skill) {
            return skill;
        }

        @Override
        public void deleteById(String skillId) {
        }
    }

    private static class FakeQuestionMapper implements QuestionMapper {
        @Override
        public QuestionResponse toResponse(Question question) {
            return QuestionResponse.builder()
                    .questionId(question.getQuestionId())
                    .subjectId(question.getSubjectId())
                    .chapterId(question.getChapterId())
                    .content(question.getContent())
                    .explanation(question.getExplanation())
                    .difficultyB(question.getDifficultyB())
                    .build();
        }

        @Override
        public List<QuestionResponse> toResponseList(List<Question> questions) {
            return questions.stream().map(this::toResponse).toList();
        }

        @Override
        public OptionResponse toOptionResponse(Option option) {
            return OptionResponse.builder()
                    .optionId(option.getOptionId())
                    .content(option.getContent())
                    .corrected(option.isCorrected())
                    .build();
        }

        @Override
        public List<OptionResponse> toOptionResponseList(List<Option> options) {
            return options.stream().map(this::toOptionResponse).toList();
        }
    }
}

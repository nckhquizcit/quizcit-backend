package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaRequest;
import vn.id.quiz.quizcitbackend.modules.ai.dto.SkillDeltaItem;
import vn.id.quiz.quizcitbackend.modules.ai.service.AIService;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaUserUnlockedChapterRepository;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaChapterSkillRepository;
import vn.id.quiz.quizcitbackend.modules.content.service.SubjectVisibilityGuard;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.AbilityHistory;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.QuestionSkill;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.Skill;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.UserSkillAbility;
import vn.id.quiz.quizcitbackend.modules.progress.repository.ProgressRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.adaptive.AdaptiveEligibilityResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.adaptive.AdaptiveQuizResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.option.OptionResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.QuestionResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.QuestionSkillResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.SubmitAnswerRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Attempt;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.AdaptiveQuestionSelection;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Option;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizQuestion;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizType;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Response;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.UserAdaptiveQuizTracking;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence.JpaAdaptiveQuestionSelectionRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence.JpaUserAdaptiveQuizTrackingRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.SkillRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

@Service
@RequiredArgsConstructor(onConstructor_ = @org.springframework.beans.factory.annotation.Autowired)
public class AdaptiveQuizService {
    private static final int PAGE_SIZE = 5;
    private static final int INITIAL_BUFFER_SIZE = 10;

    private final QuizRepository quizRepository;
    private final ContentRepository contentRepository;
    private final ProgressRepository progressRepository;
    private final SkillRepository skillRepository;
    private final JpaUserUnlockedChapterRepository unlockedChapterRepository;
    private final QuestionMapper questionMapper;
    private final AIService aiService;
    private final JpaChapterSkillRepository chapterSkillRepository;
    private final JpaAdaptiveQuestionSelectionRepository adaptiveQuestionSelectionRepository;
    private final SubjectVisibilityGuard visibilityGuard;
    private final JpaUserAdaptiveQuizTrackingRepository adaptiveQuizTrackingRepository;
    private final vn.id.quiz.quizcitbackend.modules.ai.service.AdviceGeneratorTask adviceGeneratorTask;

    /** Compatibility constructor for isolated service tests. */
    AdaptiveQuizService(
            QuizRepository quizRepository,
            ContentRepository contentRepository,
            ProgressRepository progressRepository,
            SkillRepository skillRepository,
            JpaUserUnlockedChapterRepository unlockedChapterRepository,
            QuestionMapper questionMapper,
            AIService aiService,
            JpaChapterSkillRepository chapterSkillRepository,
            JpaAdaptiveQuestionSelectionRepository adaptiveQuestionSelectionRepository) {
        this(quizRepository, contentRepository, progressRepository, skillRepository,
                unlockedChapterRepository, questionMapper, aiService, chapterSkillRepository,
                adaptiveQuestionSelectionRepository,
                new SubjectVisibilityGuard(contentRepository, quizRepository),
                null, null);
    }

    public AdaptiveQuizService(
            QuizRepository quizRepository,
            ContentRepository contentRepository,
            ProgressRepository progressRepository,
            SkillRepository skillRepository,
            JpaUserUnlockedChapterRepository unlockedChapterRepository,
            QuestionMapper questionMapper,
            AIService aiService,
            JpaChapterSkillRepository chapterSkillRepository,
            JpaAdaptiveQuestionSelectionRepository adaptiveQuestionSelectionRepository,
            SubjectVisibilityGuard visibilityGuard) {
        this(quizRepository, contentRepository, progressRepository, skillRepository,
                unlockedChapterRepository, questionMapper, aiService, chapterSkillRepository,
                adaptiveQuestionSelectionRepository,
                visibilityGuard,
                null, null);
    }

    @Transactional(readOnly = true)
    public AdaptiveEligibilityResponse getEligibility(String userId, String subjectId) {
        visibilityGuard.requireAccessibleSubject(subjectId);
        int required = UserAdaptiveQuizTracking.DEFAULT_REQUIRED_STANDARD_QUIZZES;
        int completed = 0;

        if (adaptiveQuizTrackingRepository != null) {
            var tracking = adaptiveQuizTrackingRepository.findByUserIdAndSubjectId(userId, subjectId);
            if (tracking.isPresent()) {
                completed = tracking.get().getCompletedStandardQuizzes();
                required = tracking.get().getRequiredStandardQuizzes();
            }
        }

        int remaining = Math.max(0, required - completed);
        boolean eligible = visibilityGuard.isAdministrator()
                || (adaptiveQuizTrackingRepository == null || completed >= required);

        return AdaptiveEligibilityResponse.builder()
                .subjectId(subjectId)
                .requiredQuizzes(required)
                .completedQuizzes(completed)
                .remainingQuizzes(remaining)
                .eligible(eligible)
                .build();
    }

    @Transactional
    public void recordStandardQuizCompleted(String userId, String subjectId) {
        if (userId != null && !userId.isBlank() && subjectId != null && !subjectId.isBlank()
                && adaptiveQuizTrackingRepository != null) {
            adaptiveQuizTrackingRepository.incrementCompletedStandardQuizzes(userId, subjectId);
        }
    }

    @Transactional
    public AdaptiveQuizResponse start(String userId, String subjectId, int totalQuestion) {
        validateTotal(totalQuestion);
        visibilityGuard.requireAccessibleSubject(subjectId);

        AdaptiveEligibilityResponse eligibility = getEligibility(userId, subjectId);
        if (!visibilityGuard.isAdministrator() && !eligibility.isEligible()) {
            throw new AppException(
                    "Bạn cần hoàn thành tối thiểu " + eligibility.getRequiredQuizzes()
                            + " bài kiểm tra khác (Chương hoặc Tổng kết) trước khi làm bài thích ứng. Còn lại: "
                            + eligibility.getRemainingQuizzes() + " bài.",
                    HttpStatus.FORBIDDEN);
        }

        List<Chapter> unlocked = unlockedChapters(userId, subjectId);
        if (unlocked.isEmpty()) {
            throw new AppException("Bạn chưa mở khóa chương nào trong môn học này", HttpStatus.BAD_REQUEST);
        }
        ensureEnoughEligibleQuestions(userId, unlocked, totalQuestion);

        Quiz quiz = Quiz.create(subjectId, unlocked.getFirst().getChapterId(),
                "Bài kiểm tra thích ứng", totalQuestion, QuizType.ADAPTIVE, 0, 1);
        quizRepository.save(quiz);
        Attempt attempt = Attempt.create(userId, quiz.getQuizId(), totalQuestion);
        quizRepository.save(attempt);
        appendQuestions(attempt, unlocked, Math.min(INITIAL_BUFFER_SIZE, totalQuestion));
        return state(attempt, quiz);
    }

    @Transactional(readOnly = true)
    public AdaptiveQuizResponse getState(String userId, String attemptId) {
        return state(requireAttempt(userId, attemptId));
    }

    @Transactional
    public AdaptiveQuizResponse submitBatch(String userId, String attemptId, List<SubmitAnswerRequest> answers) {
        Attempt locked = quizRepository.findAttemptByIdForUpdate(attemptId)
                .orElseThrow(() -> new AppException("Không tìm thấy attempt", HttpStatus.NOT_FOUND));
        Attempt attempt = requireAttempt(userId, locked);
        List<Response> existing = quizRepository.findResponsesByAttemptId(attemptId);
        if (existing.size() >= attempt.getTotalQuestion()) {
            return state(attempt);
        }

        List<QuizQuestion> assigned = quizRepository.findQuizQuestionsByQuizId(attempt.getQuizId());
        Set<String> answeredIds = existing.stream().map(Response::getQuestionId).collect(Collectors.toSet());
        List<String> expected = assigned.stream().map(QuizQuestion::getQuestionId)
                .filter(id -> !answeredIds.contains(id)).limit(PAGE_SIZE).toList();
        validateBatch(expected, answers);

        Map<String, String> submitted = answers.stream().collect(Collectors.toMap(
                SubmitAnswerRequest::getQuestionId, SubmitAnswerRequest::getOptionId, (a, b) -> a));
        Map<String, Question> questions = quizRepository.findQuestionsByIds(expected).stream()
                .collect(Collectors.toMap(Question::getQuestionId, q -> q));
        Map<String, List<Option>> options = quizRepository.findOptionsByQuestionIds(expected).stream()
                .collect(Collectors.groupingBy(Option::getQuestionId));
        List<Response> saved = new ArrayList<>();
        for (String questionId : expected) {
            String optionId = submitted.get(questionId);
            Option selected = optionId == null || optionId.isBlank() ? null
                    : options.getOrDefault(questionId, List.of()).stream()
                            .filter(option -> option.getOptionId().equals(optionId)).findFirst()
                            .orElseThrow(() -> new AppException("Đáp án không thuộc câu hỏi", HttpStatus.BAD_REQUEST));
            saved.add(Response.create(attemptId, questionId, selected == null ? null : selected.getOptionId(),
                    selected != null && selected.isCorrected()));
        }
        quizRepository.saveResponses(saved);
        updateTheta(attempt, questions, saved);

        int answeredCount = existing.size() + saved.size();
        if (answeredCount == attempt.getTotalQuestion()) {
            complete(attempt);
            return state(attempt);
        }

        int unansweredBuffered = assigned.size() - answeredCount;
        // Luôn giữ sẵn 10 câu chưa làm: 5 câu đang hiển thị và 5 câu kế
        // tiếp được chọn bằng theta vừa cập nhật.
        if (unansweredBuffered <= PAGE_SIZE && assigned.size() < attempt.getTotalQuestion()) {
            appendQuestions(attempt, unlockedChapters(userId,
                    quizRepository.findQuizById(attempt.getQuizId()).orElseThrow().getSubjectId()), PAGE_SIZE);
        }
        return state(attempt);
    }

    static void validateTotal(int total) {
        if (total < 15 || total % PAGE_SIZE != 0) {
            throw new AppException("Số câu phải từ 15 trở lên và chia hết cho 5", HttpStatus.BAD_REQUEST);
        }
    }

    private List<Chapter> unlockedChapters(String userId, String subjectId) {
        List<Chapter> chapters = contentRepository.findAllChapter(subjectId);
        Set<String> unlockedIds = visibilityGuard != null
                ? visibilityGuard.getUnlockedChapterIds(userId, subjectId)
                : unlockedChapterRepository
                        .findByUserIdAndChapterIdIn(userId, chapters.stream().map(Chapter::getChapterId).toList())
                        .stream().map(value -> value.getChapterId()).collect(Collectors.toSet());
        return chapters.stream().filter(chapter -> unlockedIds.contains(chapter.getChapterId())).toList();
    }

    private void ensureEnoughEligibleQuestions(String userId, List<Chapter> chapters, int required) {
        if (eligibleQuestions(userId, chapters, Set.of()).size() < required) {
            throw new AppException("Ngân hàng không có đủ " + required
                    + " câu thuộc chương đã mở khóa và khớp kỹ năng của bạn", HttpStatus.BAD_REQUEST);
        }
    }

    private void appendQuestions(Attempt attempt, List<Chapter> chapters, int requested) {
        List<QuizQuestion> current = quizRepository.findQuizQuestionsByQuizId(attempt.getQuizId());
        Set<String> excluded = current.stream().map(QuizQuestion::getQuestionId).collect(Collectors.toSet());
        List<AdaptiveCandidate> selected = eligibleQuestions(attempt.getUserId(), chapters, excluded).stream()
                .limit(Math.min(requested, attempt.getTotalQuestion() - current.size())).toList();
        if (selected.size() < Math.min(requested, attempt.getTotalQuestion() - current.size())) {
            throw new AppException("Không còn đủ câu hỏi phù hợp cho bài thích ứng", HttpStatus.CONFLICT);
        }
        int index = current.size() + 1;
        ZonedDateTime selectedAt = ZonedDateTime.now();
        List<AdaptiveQuestionSelection> snapshots = new ArrayList<>();
        for (AdaptiveCandidate candidate : selected) {
            Question question = candidate.question();
            int questionOrder = index++;
            int batchNumber = (questionOrder - 1) / PAGE_SIZE + 1;
            QuestionSkill drivingSkill = candidate.drivingSkill();
            quizRepository.save(QuizQuestion.create(attempt.getQuizId(), question.getQuestionId(), questionOrder));
            for (QuestionSkill skill : candidate.comparedSkills()) {
                double skillTheta = candidate.thetaBySkill().get(skill.getSkillId());
                snapshots.add(AdaptiveQuestionSelection.create(
                        attempt.getAttemptId(), attempt.getUserId(), question.getQuestionId(), skill.getSkillId(),
                        skillTheta, question.getDifficultyB(), skill.isPrimary(),
                        skill.getSkillId().equals(drivingSkill.getSkillId()), batchNumber, questionOrder, selectedAt));
            }
        }
        adaptiveQuestionSelectionRepository.saveAll(snapshots);
    }

    private List<AdaptiveCandidate> eligibleQuestions(String userId, List<Chapter> chapters, Set<String> excluded) {
        Map<String, Double> theta = progressRepository.findUserSkillAbilities(userId).stream()
                .collect(Collectors.toMap(UserSkillAbility::getSkillId,
                        ability -> ability.getTheta() == null ? 0.0 : ability.getTheta(), (a, b) -> a));
        if (theta.isEmpty()) return List.of();
        
        List<Question> pool = quizRepository.findAnswerableQuestionsByChapterIds(
                chapters.stream().map(Chapter::getChapterId).toList()).stream()
                .filter(question -> !excluded.contains(question.getQuestionId())).collect(Collectors.toCollection(ArrayList::new));
                
        Collections.shuffle(pool); // trộn trước mỗi lần chọn để tie không thiên vị thứ tự DB
        Map<String, Set<String>> chapterSkillIds = chapterSkillRepository.findByChapterIdIn(
                chapters.stream().map(Chapter::getChapterId).toList()).stream()
                .collect(Collectors.groupingBy(value -> value.getChapterId(),
                        Collectors.mapping(value -> value.getSkillId(), Collectors.toSet())));
        Map<String, String> chapterByQuestionId = pool.stream().collect(Collectors.toMap(
                Question::getQuestionId, Question::getChapterId));
        Map<String, List<QuestionSkill>> mappings = progressRepository
                .findQuestionSkillsByQuestionIds(pool.stream().map(Question::getQuestionId).toList()).stream()
                .filter(mapping -> theta.containsKey(mapping.getSkillId()))
                .filter(mapping -> chapterSkillIds
                        .getOrDefault(chapterByQuestionId.get(mapping.getQuestionId()), Set.of())
                        .contains(mapping.getSkillId()))
                .collect(Collectors.groupingBy(QuestionSkill::getQuestionId));
        return pool.stream().filter(question -> mappings.containsKey(question.getQuestionId()))
                .map(question -> AdaptiveCandidate.create(question, mappings.get(question.getQuestionId()), theta))
                .sorted(Comparator
                        .comparing((AdaptiveCandidate candidate) -> candidate.comparedSkills().stream()
                                .noneMatch(QuestionSkill::isPrimary))
                        .thenComparingDouble(AdaptiveCandidate::distance))
                .toList();
    }

    private record AdaptiveCandidate(
            Question question,
            List<QuestionSkill> comparedSkills,
            Map<String, Double> thetaBySkill,
            QuestionSkill drivingSkill,
            double distance) {

        private static AdaptiveCandidate create(
                Question question,
                List<QuestionSkill> matchingSkills,
                Map<String, Double> thetaBySkill) {
            List<QuestionSkill> primary = matchingSkills.stream().filter(QuestionSkill::isPrimary).toList();
            List<QuestionSkill> compared = primary.isEmpty() ? List.copyOf(matchingSkills) : primary;
            QuestionSkill driving = compared.stream()
                    .min(Comparator.comparingDouble((QuestionSkill skill) -> Math.abs(
                                    thetaBySkill.get(skill.getSkillId()) - question.getDifficultyB()))
                            .thenComparing(QuestionSkill::getSkillId))
                    .orElseThrow();
            double distance = Math.abs(thetaBySkill.get(driving.getSkillId()) - question.getDifficultyB());
            return new AdaptiveCandidate(question, compared, thetaBySkill, driving, distance);
        }
    }

    /**
     * Nếu câu có primary skill trùng với user thì dùng các primary skill để
     * đánh giá trước; nếu không có mới fallback sang các skill trùng còn lại.
     * Trong nhóm được chọn, một skill đủ gần là đủ để câu hỏi hợp lệ.
     */
    static double matchingThetaDistance(
            Question question,
            List<QuestionSkill> matchingSkills,
            Map<String, Double> thetaBySkill) {
        List<QuestionSkill> primary = matchingSkills.stream()
                .filter(QuestionSkill::isPrimary)
                .toList();
        List<QuestionSkill> compared = primary.isEmpty() ? matchingSkills : primary;
        return compared.stream()
                .mapToDouble(mapping -> Math.abs(
                        thetaBySkill.get(mapping.getSkillId()) - question.getDifficultyB()))
                .min()
                .orElse(Double.MAX_VALUE);
    }

    private void validateBatch(List<String> expected, List<SubmitAnswerRequest> answers) {
        if (expected.isEmpty()) throw new AppException("Không còn câu hỏi để nộp", HttpStatus.CONFLICT);
        if (answers == null || answers.size() != expected.size()) {
            throw new AppException("Phải nộp đúng 5 câu đang hiển thị", HttpStatus.BAD_REQUEST);
        }
        List<String> submitted = answers.stream().map(SubmitAnswerRequest::getQuestionId).toList();
        if (submitted.stream().distinct().count() != submitted.size() || !new LinkedHashSet<>(expected).equals(new LinkedHashSet<>(submitted))) {
            throw new AppException("Danh sách câu trả lời không khớp batch hiện tại", HttpStatus.BAD_REQUEST);
        }
    }

    private static final double DEFAULT_THETA_VARIANCE = 1.0;

    private void updateTheta(Attempt attempt, Map<String, Question> questions, List<Response> responses) {
        Map<String, Response> responseMap = responses.stream().collect(Collectors.toMap(Response::getQuestionId, r -> r));
        Map<String, UserSkillAbility> abilitiesBySkill = progressRepository
                .findUserSkillAbilities(attempt.getUserId()).stream()
                .collect(Collectors.toMap(UserSkillAbility::getSkillId, ability -> ability));
                
        // Lọc đi dòng filter existingSkillIds để skill mới cũng được track
        Map<String, List<QuestionSkill>> mappings = progressRepository
                .findQuestionSkillsByQuestionIds(questions.keySet()).stream()
                .collect(Collectors.groupingBy(QuestionSkill::getSkillId));
                
        Map<String, EstimateThetaRequest> requests = new LinkedHashMap<>();
        mappings.forEach((skillId, values) -> {
            UserSkillAbility ability = abilitiesBySkill.get(skillId);
            double oldTheta = (ability != null && ability.getTheta() != null) ? ability.getTheta() : 0.0;
            double oldThetaVar = (ability != null && ability.getThetaVariance() != null) ? ability.getThetaVariance() : DEFAULT_THETA_VARIANCE;
            requests.put(skillId, new EstimateThetaRequest(
                oldTheta,
                oldThetaVar,
                values.stream().map(value -> responseMap.get(value.getQuestionId()).isScore() ? 1 : 0).toList(),
                values.stream().map(value -> questions.get(value.getQuestionId()).getDifficultyB()).toList(),
                values.stream().map(QuestionSkill::getWeight).toList()));
        });
        
        Map<String, vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaResponse> estimated = aiService.estimateThetaBatch(requests);
        List<UserSkillAbility> abilitiesToSave = new ArrayList<>();
        List<AbilityHistory> historiesToSave = new ArrayList<>();
        
        estimated.forEach((skillId, result) -> {
            UserSkillAbility ability = abilitiesBySkill.computeIfAbsent(skillId,
                    ignored -> UserSkillAbility.create(attempt.getUserId(), skillId));
            double old = ability.getTheta() == null ? 0 : ability.getTheta();
            
            ability.updateAbility(result.theta(), result.thetaVariance());
            abilitiesToSave.add(ability);
            historiesToSave.add(AbilityHistory.create(
                    attempt.getUserId(), skillId, result.theta(), result.theta() - old, attempt.getAttemptId()));
        });

        progressRepository.saveUserSkillAbilities(abilitiesToSave);
        progressRepository.saveAbilityHistories(historiesToSave);
    }

    private void complete(Attempt attempt) {
        List<Response> responses = quizRepository.findResponsesByAttemptId(attempt.getAttemptId());
        Set<String> questionIds = responses.stream().map(Response::getQuestionId).collect(Collectors.toSet());
        Set<String> skillIds = progressRepository.findQuestionSkillsByQuestionIds(questionIds).stream()
                .map(QuestionSkill::getSkillId).collect(Collectors.toSet());
        List<Double> theta = progressRepository.findUserSkillAbilities(attempt.getUserId()).stream()
                .filter(ability -> skillIds.contains(ability.getSkillId()))
                .map(ability -> ability.getTheta() == null ? 0 : ability.getTheta()).toList();
        attempt.complete(responses.stream().filter(Response::isScore).count(),
                theta.stream().mapToDouble(Double::doubleValue).average().orElse(0));

        // Sinh lời khuyên dựa trên delta theta MỚI NHẤT của từng skill xuất hiện
        // trong toàn bộ attempt này (adaptive quiz cập nhật theta theo từng batch
        // 5 câu, nên phải gom theo skillId rồi lấy bản ghi mới nhất).
        if (adviceGeneratorTask != null) {
            adviceGeneratorTask.generateAndAttachAdvice(attempt.getAttemptId(), buildSkillDeltaItemsForAttempt(attempt.getAttemptId()));
        }

        quizRepository.save(attempt);

        Quiz quiz = quizRepository.findQuizById(attempt.getQuizId()).orElse(null);
        if (quiz != null && adaptiveQuizTrackingRepository != null) {
            adaptiveQuizTrackingRepository.resetCompletedStandardQuizzes(attempt.getUserId(), quiz.getSubjectId());
        }
    }

    /**
     * Lấy delta theta mới nhất của từng skill xuất hiện trong attempt (gom theo
     * skillId, giữ bản ghi createdAt lớn nhất — giống cách
     * AttemptServiceImplement.getResult tổng hợp lịch sử cho quiz cố định) để
     * làm input cho AI sinh lời khuyên.
     */
    private List<SkillDeltaItem> buildSkillDeltaItemsForAttempt(String attemptId) {
        List<AbilityHistory> histories = progressRepository.findAbilityHistoriesByAttemptId(attemptId);
        if (histories == null || histories.isEmpty()) {
            return List.of();
        }
        Map<String, AbilityHistory> latestBySkill = histories.stream()
                .collect(Collectors.toMap(
                        AbilityHistory::getSkillId,
                        history -> history,
                        (first, second) -> first.getCreatedAt().isAfter(second.getCreatedAt()) ? first : second,
                        LinkedHashMap::new));

        Map<String, Double> totalDeltaBySkill = histories.stream()
                .collect(Collectors.groupingBy(
                        AbilityHistory::getSkillId,
                        Collectors.summingDouble(AbilityHistory::getDelta)));

        Map<String, String> skillNames = skillRepository.findSkillNamesByIds(latestBySkill.keySet());
        if (skillNames == null || skillNames.isEmpty()) {
            List<Skill> skills = skillRepository.findByIds(latestBySkill.keySet());
            skillNames = (skills == null ? List.<Skill>of() : skills).stream()
                    .collect(Collectors.toMap(Skill::getSkillId, Skill::getName));
        }
        Map<String, String> finalSkillNames = skillNames;

        return latestBySkill.values().stream()
                .map(history -> {
                    double totalDelta = totalDeltaBySkill.getOrDefault(history.getSkillId(), history.getDelta());
                    return new SkillDeltaItem(
                            finalSkillNames.getOrDefault(history.getSkillId(), history.getSkillId()),
                            history.getTheta() - totalDelta,
                            history.getTheta(),
                            totalDelta);
                })
                .toList();
    }
    private Attempt requireAttempt(String userId, String attemptId) {
        Attempt attempt = quizRepository.findAttemptById(attemptId)
                .orElseThrow(() -> new AppException("Không tìm thấy attempt", HttpStatus.NOT_FOUND));
        return requireAttempt(userId, attempt);
    }

    private Attempt requireAttempt(String userId, Attempt attempt) {
        Quiz quiz = quizRepository.findQuizById(attempt.getQuizId()).orElseThrow();
        if (!attempt.getUserId().equals(userId)) throw new AppException("Bạn không có quyền truy cập attempt này", HttpStatus.FORBIDDEN);
        if (quiz.getType() != QuizType.ADAPTIVE) throw new AppException("Đây không phải bài thích ứng", HttpStatus.BAD_REQUEST);
        if (visibilityGuard.isCourseEnrollmentCheckEnabled()) {
            visibilityGuard.requireAccessibleSubject(quiz.getSubjectId());
        }
        return attempt;
    }

    private AdaptiveQuizResponse state(Attempt attempt) {
        return state(attempt, quizRepository.findQuizById(attempt.getQuizId()).orElse(null));
    }

    private AdaptiveQuizResponse state(Attempt attempt, Quiz quiz) {
        List<Response> responses = quizRepository.findResponsesByAttemptId(attempt.getAttemptId());
        Set<String> answered = responses.stream().map(Response::getQuestionId).collect(Collectors.toSet());
        List<String> nextIds = quizRepository.findQuizQuestionsByQuizId(attempt.getQuizId()).stream()
                .map(QuizQuestion::getQuestionId).filter(id -> !answered.contains(id)).limit(PAGE_SIZE).toList();

        String quizName = quiz != null ? quiz.getName() : null;
        String subjectName = null;
        if (quiz != null && quiz.getSubjectId() != null && contentRepository != null) {
            subjectName = contentRepository.findSubjectById(quiz.getSubjectId())
                    .map(vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject::getName)
                    .orElse(null);
        }

        return AdaptiveQuizResponse.builder()
                .quizId(attempt.getQuizId())
                .attemptId(attempt.getAttemptId())
                .quizName(quizName)
                .subjectName(subjectName)
                .totalQuestion(attempt.getTotalQuestion())
                .answeredQuestion(responses.size())
                .page(responses.size() >= attempt.getTotalQuestion()
                        ? attempt.getTotalQuestion() / PAGE_SIZE
                        : responses.size() / PAGE_SIZE + 1)
                .completed(responses.size() >= attempt.getTotalQuestion())
                .questions(toQuestionResponses(nextIds))
                .build();
    }

    private List<QuestionResponse> toQuestionResponses(Collection<String> ids) {
        Map<String, Question> questions = quizRepository.findQuestionsByIds(ids).stream()
                .collect(Collectors.toMap(Question::getQuestionId, q -> q));
        Map<String, List<Option>> options = quizRepository.findOptionsByQuestionIds(ids).stream()
                .collect(Collectors.groupingBy(Option::getQuestionId));
        Map<String, List<QuestionSkill>> mappings = progressRepository.findQuestionSkillsByQuestionIds(ids).stream()
                .collect(Collectors.groupingBy(QuestionSkill::getQuestionId));
        Map<String, String> skillNames = skillRepository.findByIds(mappings.values().stream().flatMap(List::stream)
                .map(QuestionSkill::getSkillId).distinct().toList()).stream()
                .collect(Collectors.toMap(skill -> skill.getSkillId(), skill -> skill.getName()));
        return ids.stream().map(id -> {
            QuestionResponse result = questionMapper.toResponse(questions.get(id));
            result.setOptions(options.getOrDefault(id, List.of()).stream().map(option -> OptionResponse.builder()
                    .optionId(option.getOptionId()).content(option.getContent())
                    .contentFormat(questions.get(id).getContentFormat()).corrected(false).build()).toList());
            result.setSkills(mappings.getOrDefault(id, List.of()).stream().map(mapping -> QuestionSkillResponse.builder()
                    .skillId(mapping.getSkillId()).name(skillNames.get(mapping.getSkillId()))
                    .weight(mapping.getWeight()).primary(mapping.isPrimary()).build()).toList());
            return result;
        }).toList();
    }
}

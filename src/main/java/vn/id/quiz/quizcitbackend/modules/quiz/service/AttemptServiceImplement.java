package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Comparator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.content.service.SubjectVisibilityGuard;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaRequest;
import vn.id.quiz.quizcitbackend.modules.ai.dto.SkillDeltaItem;
import vn.id.quiz.quizcitbackend.modules.ai.service.AIService;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.AbilityHistory;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.QuestionSkill;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.Skill;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.UserSkillAbility;
import vn.id.quiz.quizcitbackend.modules.progress.repository.ProgressRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.SkillRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.option.OptionResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.ActiveAttemptResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.AttemptStateResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.QuestionResultResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.QuizResultResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.skill.SkillResultResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.skill.SkillThetaData;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.StartQuizResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.SubmitAnswerRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.SubmitQuizRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Attempt;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.AttemptStatus;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Option;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizQuestion;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizType;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Response;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AttemptServiceImplement
        implements AttemptService {

    private final QuizRepository quizRepository;
    private final ContentRepository contentRepository;
    private final AttemptMapper mapper;
    private final ProgressRepository progressRepository;
    private final SkillRepository skillRepository;

    private final AIService aiService;
    private final AdaptiveQuizService adaptiveQuizService;
    private final SubjectVisibilityGuard visibilityGuard;
    private final vn.id.quiz.quizcitbackend.modules.ai.service.AdviceGeneratorTask adviceGeneratorTask;

    // Giữ constructor này để tương thích các unit test/service consumer cũ.
    AttemptServiceImplement(
            QuizRepository quizRepository,
            ContentRepository contentRepository,
            AttemptMapper mapper,
            ProgressRepository progressRepository,
            SkillRepository skillRepository,
            AIService aiService) {
        this(quizRepository, contentRepository, mapper, progressRepository,
                skillRepository, aiService, null,
                new SubjectVisibilityGuard(contentRepository, quizRepository), null);
    }

    @Override
    @Transactional
    public StartQuizResponse startQuiz(
            String userId,
            String quizId) {

        visibilityGuard.requireVisibleQuiz(quizId);

        Quiz quiz = quizRepository.findQuizById(quizId)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy đề thi",
                        HttpStatus.NOT_FOUND));

        if (quiz.getType() == QuizType.ADAPTIVE) {
            throw new AppException(
                    "Bài thích ứng phải được bắt đầu từ môn học",
                    HttpStatus.BAD_REQUEST);
        }

        Optional<Attempt> activeAttempt = quizRepository.findLatestInProgressAttempt(userId, quizId);
        if (activeAttempt.isPresent()) {
            return mapper.toStartQuizResponse(activeAttempt.get());
        }

        List<QuizQuestion> questions = quizRepository.findQuizQuestionsByQuizId(
                quizId);

        if (questions.isEmpty()) {
            throw new AppException(
                    "Đề thi không có câu hỏi",
                    HttpStatus.BAD_REQUEST);
        }

        Attempt attempt = Attempt.create(
                userId,
                quizId,
                questions.size(),
                quiz.getDuration());

        quizRepository.save(attempt);

        return mapper.toStartQuizResponse(attempt);
    }

    @Override
    @Transactional(readOnly = true)
    public AttemptStateResponse getAttemptState(String attemptId) {
        Attempt attempt = quizRepository.findAttemptById(attemptId)
                .orElseThrow(() -> new AppException("Không tìm thấy attempt", HttpStatus.NOT_FOUND));
        return AttemptStateResponse.builder()
                .attemptId(attempt.getAttemptId())
                .quizId(attempt.getQuizId())
                .totalQuestion(attempt.getTotalQuestion())
                .status(attempt.getStatus())
                .deadlineAt(attempt.getDeadlineAt())
                .answers(Map.copyOf(attempt.getDraftAnswers()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActiveAttemptResponse> getActiveAttempts(String userId) {
        ZonedDateTime now = ZonedDateTime.now();
        List<Attempt> active = quizRepository.findInProgressAttemptsByUserId(userId).stream()
                .filter(attempt -> attempt.getDeadlineAt() == null || attempt.getDeadlineAt().isAfter(now))
                .toList();
        if (active.isEmpty()) {
            return List.of();
        }

        Map<String, Quiz> quizById = quizRepository.findQuizzesByIds(
                        active.stream().map(Attempt::getQuizId).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(Quiz::getQuizId, quiz -> quiz, (first, ignored) -> first));
        List<Attempt> resumable = active.stream()
                .filter(attempt -> {
                    Quiz quiz = quizById.get(attempt.getQuizId());
                    return quiz != null && quiz.getType() != QuizType.ADAPTIVE;
                })
                .sorted(Comparator.comparing(Attempt::getDeadlineAt,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        if (resumable.isEmpty()) {
            return List.of();
        }

        Map<String, Quiz> resumableQuizById = resumable.stream()
                .map(attempt -> quizById.get(attempt.getQuizId()))
                .collect(Collectors.toMap(Quiz::getQuizId, quiz -> quiz, (first, ignored) -> first));
        Map<String, String> subjectNameById = contentRepository.findSubjectsByIds(
                        resumableQuizById.values().stream().map(Quiz::getSubjectId).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(Subject::getSubjectId, Subject::getName, (first, ignored) -> first));

        return resumable.stream()
                .map(attempt -> ActiveAttemptResponse.builder()
                        .attemptId(attempt.getAttemptId())
                        .quizId(attempt.getQuizId())
                        .quizName(resumableQuizById.get(attempt.getQuizId()).getName())
                        .subjectName(subjectNameById.getOrDefault(
                                resumableQuizById.get(attempt.getQuizId()).getSubjectId(), "Môn học"))
                        .deadlineAt(attempt.getDeadlineAt())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void saveDraft(String userId, String attemptId, Map<String, String> answers) {
        Attempt attempt = quizRepository.findAttemptByIdForUpdate(attemptId)
                .orElseThrow(() -> new AppException("Không tìm thấy attempt", HttpStatus.NOT_FOUND));
        if (!attempt.getUserId().equals(userId)) {
            throw new AppException("Bạn không có quyền thực hiện thao tác này.", HttpStatus.FORBIDDEN);
        }
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS
                || (attempt.getDeadlineAt() != null && !attempt.getDeadlineAt().isAfter(ZonedDateTime.now()))) {
            throw new AppException("Đã hết thời gian làm bài; hệ thống đang nộp bài.", HttpStatus.CONFLICT);
        }

        Set<String> questionIds = quizRepository.findQuizQuestionsByQuizId(attempt.getQuizId()).stream()
                .map(QuizQuestion::getQuestionId)
                .collect(Collectors.toSet());
        Map<String, String> selectedAnswers = new HashMap<>();
        for (var answer : answers.entrySet()) {
            if (!questionIds.contains(answer.getKey())) {
                throw new AppException("Câu trả lời không thuộc đề thi đang làm", HttpStatus.BAD_REQUEST);
            }
            if (answer.getValue() != null && !answer.getValue().isBlank()) {
                selectedAnswers.put(answer.getKey(), answer.getValue());
            }
        }

        Map<String, Set<String>> optionsByQuestion = quizRepository.findOptionsByQuestionIds(questionIds).stream()
                .collect(Collectors.groupingBy(Option::getQuestionId,
                        Collectors.mapping(Option::getOptionId, Collectors.toSet())));
        for (var answer : selectedAnswers.entrySet()) {
            if (!optionsByQuestion.getOrDefault(answer.getKey(), Set.of()).contains(answer.getValue())) {
                throw new AppException("Đáp án đã chọn không thuộc câu hỏi trong đề thi", HttpStatus.BAD_REQUEST);
            }
        }

        attempt.saveDraftAnswers(selectedAnswers);
        quizRepository.save(attempt);
    }

    @Override
    @Transactional
    public QuizResultResponse submitQuiz(
            SubmitQuizRequest request) {
        Attempt attempt = quizRepository // khóa attempt để double-submit không ghi response/history hai lần
                .findAttemptByIdForUpdate(
                        request.getAttemptId())
                .orElseThrow(
                        () -> new AppException(
                                "Không tìm thấy attempt",
                                HttpStatus.NOT_FOUND));

        visibilityGuard.requireVisibleQuiz(attempt.getQuizId());

        if (request.getQuizId() != null && !request.getQuizId().isBlank()
                && !attempt.getQuizId().equals(request.getQuizId())) {
            throw new AppException(
                    "Attempt không thuộc đề thi đang nộp",
                    HttpStatus.BAD_REQUEST);
        }// kiêm tra xem attempt và đề thi có

        Quiz submittedQuiz = quizRepository.findQuizById(attempt.getQuizId())
                .orElseThrow(() -> new AppException("Không tìm thấy đề thi", HttpStatus.NOT_FOUND));
        if (submittedQuiz.getType() == QuizType.ADAPTIVE) {
            if (adaptiveQuizService == null) {
                throw new AppException("Dịch vụ bài thích ứng chưa sẵn sàng", HttpStatus.SERVICE_UNAVAILABLE);
            }
            var adaptiveState = adaptiveQuizService.submitBatch(
                    attempt.getUserId(), attempt.getAttemptId(), request.getAnswers());
            if (!adaptiveState.isCompleted()) {
                throw new AppException(
                        "Bài thích ứng chỉ có thể nộp hoàn toàn ở nhóm 5 câu cuối",
                        HttpStatus.BAD_REQUEST);
            }
            return getResult(attempt.getAttemptId());
        }

        if (!quizRepository.findResponsesByAttemptId(attempt.getAttemptId()).isEmpty()) {
            return getResult(attempt.getAttemptId());
        }

        boolean timedOut = attempt.getDeadlineAt() != null
                && !attempt.getDeadlineAt().isAfter(ZonedDateTime.now());

        List<QuizQuestion> quizQuestions = quizRepository.findQuizQuestionsByQuizId(
                attempt.getQuizId()); //lấy danh sách câu hỏi

        Set<String> quizQuestionIds = quizQuestions.stream()
                .map(QuizQuestion::getQuestionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        // biến mấy cái question id thành set


        if (quizQuestionIds.isEmpty()) {
            throw new AppException(
                    "Đề thi không có câu hỏi",
                    HttpStatus.BAD_REQUEST);
        }

        Map<String, String> submittedAnswers = timedOut
                ? new HashMap<>(attempt.getDraftAnswers())
                : new HashMap<>();

        if (!timedOut) {
            for (SubmitAnswerRequest answer : request.getAnswers()) {
                if (!quizQuestionIds.contains(answer.getQuestionId())) {
                    throw new AppException("Câu trả lời không thuộc đề thi đang nộp", HttpStatus.BAD_REQUEST);
                }
                submittedAnswers.putIfAbsent(answer.getQuestionId(), answer.getOptionId());
            }
        }

        // Batch load: câu hỏi, option, skill mapping cho toàn bộ đề thi
        // (thay cho các truy vấn lặp trong vòng for → tránh N+1).
        Map<String, Question> questionById = quizRepository
                .findQuestionsByIds(quizQuestionIds)
                .stream()
                .collect(Collectors.toMap(
                        Question::getQuestionId,
                        q -> q,
                        (first, ignored) -> first));

        Map<String, List<Option>> optionsByQuestionId = quizRepository
                .findOptionsByQuestionIds(quizQuestionIds)
                .stream()
                .collect(Collectors.groupingBy(Option::getQuestionId));

        Map<String, List<QuestionSkill>> skillsByQuestionId = progressRepository
                .findQuestionSkillsByQuestionIds(quizQuestionIds)
                .stream()
                .collect(Collectors.groupingBy(QuestionSkill::getQuestionId));

        Map<String, SkillThetaData> skillMap = new HashMap<>();
        int correctCount = 0;
        List<Response> responsesToSave = new ArrayList<>();

        for (String questionId : quizQuestionIds) {
            Question question = questionById.get(questionId);
            if (question == null) {
                throw new AppException(
                        "Không tìm thấy câu hỏi trong đề thi",
                        HttpStatus.NOT_FOUND);
            }

            String optionId = submittedAnswers.get(questionId);
            Option option = null;
            boolean correct = false;

            if (optionId != null && !optionId.isBlank()) {
                option = optionsByQuestionId
                        .getOrDefault(questionId, List.of())
                        .stream()
                        .filter(o -> o.getOptionId().equals(optionId))
                        .findFirst()
                        .orElseThrow(() -> new AppException(
                                "Đáp án đã chọn không thuộc câu hỏi trong đề thi",
                                HttpStatus.BAD_REQUEST));

                correct = option.isCorrected();
            }

            responsesToSave.add(Response.create(
                    attempt.getAttemptId(),
                    question.getQuestionId(),
                    option == null ? null : option.getOptionId(),
                    correct));

            if (correct) {
                correctCount++;
            }

            List<QuestionSkill> mappings = skillsByQuestionId
                    .getOrDefault(questionId, List.of());

            for (QuestionSkill qs : mappings) {
                if (option == null) {
                    continue;
                }

                SkillThetaData data = skillMap.computeIfAbsent(
                        qs.getSkillId(),
                        id -> new SkillThetaData());

                data.getResponses()
                        .add(correct ? 1 : 0);

                data.getDifficulties()
                        .add(question.getDifficultyB());

                data.getWeights()
                        .add(qs.getWeight());
            }

        }

        // Lưu tất cả response trong 1 batch insert.
        quizRepository.saveResponses(responsesToSave);

        List<UserSkillAbility> loadedAbilities = progressRepository.findUserSkillAbilities(attempt.getUserId());
        if (loadedAbilities == null) {
            loadedAbilities = skillMap.keySet().stream().map(skillId -> progressRepository
                    .findUserSkillAbility(attempt.getUserId(), skillId).orElse(null))
                    .filter(java.util.Objects::nonNull).toList();
        }
        Map<String, UserSkillAbility> abilityBySkill = loadedAbilities.stream()
                .filter(ability -> skillMap.containsKey(ability.getSkillId()))
                .collect(Collectors.toMap(UserSkillAbility::getSkillId, ability -> ability));

        // Ước lượng theta cho TẤT CẢ skill trong 1 lần gọi AI (batch) thay vì
        // gọi lặp từng skill → loại bỏ N lần round-trip HTTP (rất nặng khi
        // AI Service cold start). Fallback nội bộ được áp dụng tự động cho
        // các skill mà AI Service không trả về.
        Map<String, EstimateThetaRequest> thetaRequests = new LinkedHashMap<>();
        for (var entry : skillMap.entrySet()) {
            String skillId = entry.getKey();
            SkillThetaData data = entry.getValue();
            UserSkillAbility ability = abilityBySkill.get(skillId);
            double oldTheta = ability == null || ability.getTheta() == null ? 0.0 : ability.getTheta();
            double oldThetaVar = ability == null || ability.getThetaVariance() == null ? 1.0 : ability.getThetaVariance();

            thetaRequests.put(
                    skillId,
                    new EstimateThetaRequest(
                            oldTheta,
                            oldThetaVar,
                            data.getResponses(),
                            data.getDifficulties(),
                            data.getWeights()));
        }

        Map<String, vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaResponse> estimatedThetaBySkill = thetaRequests.isEmpty()
                ? Map.of()
                : aiService.estimateThetaBatch(thetaRequests);

        Map<String, Double> skillThetaMap = new HashMap<>();
        List<UserSkillAbility> abilitiesToSave = new ArrayList<>();
        List<AbilityHistory> historiesToSave = new ArrayList<>();

        for (var entry : skillMap.entrySet()) {
            String skillId = entry.getKey();
            vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaResponse estimatedTheta = estimatedThetaBySkill.get(skillId);
            double newTheta = estimatedTheta != null ? estimatedTheta.theta() : 0.0;
            double newThetaVar = estimatedTheta != null ? estimatedTheta.thetaVariance() : 1.0;
            
            UserSkillAbility ability = abilityBySkill.get(skillId);
            double oldTheta = ability == null || ability.getTheta() == null ? 0.0 : ability.getTheta();

            if (ability == null) {
                ability = UserSkillAbility.create(attempt.getUserId(), skillId);
            }

            double blendedTheta = newTheta; // Prior is already factored in via Bayesian updating

            skillThetaMap.put(skillId, blendedTheta);
            ability.updateAbility(blendedTheta, newThetaVar);

            abilitiesToSave.add(ability);

            AbilityHistory history = AbilityHistory.create(
                    attempt.getUserId(),
                    skillId,
                    blendedTheta,
                    blendedTheta - oldTheta,
                    attempt.getAttemptId());


            historiesToSave.add(history);
        }
        if (progressRepository.saveUserSkillAbilities(abilitiesToSave) == null) {
            abilitiesToSave.forEach(progressRepository::save);
        }
        if (progressRepository.saveAbilityHistories(historiesToSave) == null) {
            historiesToSave.forEach(progressRepository::save);
        }
        double sum = 0.0;
        int count = 0;
        for (UserSkillAbility a : loadedAbilities) {
            if (!skillThetaMap.containsKey(a.getSkillId())) {
                sum += (a.getTheta() == null ? 0.0 : a.getTheta());
                count++;
            }
        }
        for (UserSkillAbility a : abilitiesToSave) {
            sum += (a.getTheta() == null ? 0.0 : a.getTheta());
            count++;
        }
        double overallTheta = count > 0 ? sum / count : 0.0;

        double rawScore = correctCount;
        attempt.complete(
                rawScore,
                overallTheta);
        if (timedOut) {
            attempt.markTimedOut();
        }

        // MỚI: sinh lời khuyên cải thiện dựa trên delta theta từng skill vừa tính,
        // lưu 1 lần tại thời điểm nộp bài (không tính lại mỗi lần xem kết quả).
        if (adviceGeneratorTask != null && !historiesToSave.isEmpty()) {
            adviceGeneratorTask.generateAndAttachAdvice(attempt.getAttemptId(), buildSkillDeltaItems(historiesToSave));
        }
        
        quizRepository.save(
                attempt);

        if (submittedQuiz != null && (submittedQuiz.getType() == QuizType.CHAPTER || submittedQuiz.getType() == QuizType.FINAL)) {
            if (adaptiveQuizService != null) {
                adaptiveQuizService.recordStandardQuizCompleted(attempt.getUserId(), submittedQuiz.getSubjectId());
            }
        }

        return getResult(attempt.getAttemptId());
    }

    /**
     * Chuyển các {@link AbilityHistory} vừa tính trong lần nộp bài này thành
     * input cho AI sinh lời khuyên. {@code oldTheta} suy ra ngược từ
     * {@code theta - delta} thay vì phải track riêng, vì
     * {@code AbilityHistory.delta = blendedTheta - oldTheta} đã được lưu sẵn.
     */
    private List<SkillDeltaItem> buildSkillDeltaItems(List<AbilityHistory> historiesToSave) {
        if (historiesToSave.isEmpty()) {
            return List.of();
        }
        Set<String> skillIds = historiesToSave.stream()
                .map(AbilityHistory::getSkillId).collect(Collectors.toSet());
        Map<String, String> skillNames = skillRepository.findSkillNamesByIds(skillIds);
        if (skillNames == null || skillNames.isEmpty()) {
            List<Skill> skills = skillRepository.findByIds(skillIds);
            if (skills == null) {
                skills = skillIds.stream().map(skillRepository::findById)
                        .flatMap(Optional::stream).toList();
            }
            skillNames = skills.stream()
                    .collect(Collectors.toMap(Skill::getSkillId, Skill::getName));
        }

        Map<String, String> finalSkillNames = skillNames;
        return historiesToSave.stream()
                .map(history -> {
                    double rawOld = history.getTheta() - history.getDelta();
                    double rawNew = history.getTheta();
                    double scaledOld = vn.id.quiz.quizcitbackend.modules.ai.utils.ThetaUtils.toScaled(rawOld);
                    double scaledNew = vn.id.quiz.quizcitbackend.modules.ai.utils.ThetaUtils.toScaled(rawNew);
                    return new SkillDeltaItem(
                            finalSkillNames.getOrDefault(history.getSkillId(), history.getSkillId()),
                            scaledOld,
                            scaledNew,
                            scaledNew - scaledOld);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public QuizResultResponse getResult(
            String attemptId) {

        Attempt attempt = quizRepository
                .findAttemptById(attemptId)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy kết quả bài thi",
                        HttpStatus.NOT_FOUND));

        visibilityGuard.requireVisibleQuiz(attempt.getQuizId());

        List<AbilityHistory> histories = progressRepository.findAbilityHistoriesByAttemptId(
                attemptId);

        // Adaptive attempts create one history row per 5-question batch. Keep
        // all rows for the progress timeline, but show only the latest value
        // of each skill in the final quiz result.
        Map<String, AbilityHistory> latestHistoryBySkill = histories.stream()
                .collect(Collectors.toMap(
                        AbilityHistory::getSkillId,
                        history -> history,
                        (first, second) -> first.getCreatedAt().isAfter(second.getCreatedAt())
                                ? first
                                : second,
                        LinkedHashMap::new));

        Map<String, Double> totalDeltaBySkill = histories.stream()
                .collect(Collectors.groupingBy(
                        AbilityHistory::getSkillId,
                        Collectors.summingDouble(AbilityHistory::getDelta)));

        Map<String, String> skillNames = skillRepository.findSkillNamesByIds(latestHistoryBySkill.keySet());
        if (skillNames == null || skillNames.isEmpty()) {
            List<Skill> loadedSkills = skillRepository.findByIds(latestHistoryBySkill.keySet());
            if (loadedSkills == null) loadedSkills = latestHistoryBySkill.keySet().stream()
                    .map(skillRepository::findById).flatMap(Optional::stream).toList();
            skillNames = loadedSkills.stream()
                    .collect(Collectors.toMap(Skill::getSkillId, Skill::getName));
        }
        Map<String, String> finalSkillNames = skillNames;
        List<SkillResultResponse> skillResults = latestHistoryBySkill.values().stream()
                .map(history -> SkillResultResponse.builder()
                        .skillId(history.getSkillId())
                        .skillName(finalSkillNames.get(history.getSkillId()))
                        .theta(history.getTheta())
                        .delta(totalDeltaBySkill.getOrDefault(history.getSkillId(), history.getDelta()))
                        .build())
                .toList();
        Quiz quiz = quizRepository.findQuizById(attempt.getQuizId())
                .orElse(null);
        Subject subject = quiz == null
                ? null
                : contentRepository.findSubjectById(quiz.getSubjectId())
                        .orElse(null);
        String resolvedQuizName = quiz != null && quiz.getName() != null && !quiz.getName().isBlank()
                ? quiz.getName()
                : "Bài kiểm tra";
        String resolvedSubjectName = subject != null && subject.getName() != null && !subject.getName().isBlank()
                ? subject.getName()
                : "Môn học";
        int durationMinutes = quiz == null ? 0 : quiz.getDuration();

        return QuizResultResponse.builder()
                .attemptId(attempt.getAttemptId())
                .quizId(attempt.getQuizId())
                .quizName(resolvedQuizName)
                .subjectName(resolvedSubjectName)
                .durationMinutes(durationMinutes)
                .submittedAt(attempt.getSubmittedAt() == null ? attempt.getCreatedAt() : attempt.getSubmittedAt())
                .rawScore(attempt.getRawScore())
                .totalQuestion(attempt.getTotalQuestion())
                .overallTheta(attempt.getOverallTheta())
                .skillResults(skillResults)
                .questionResults(buildQuestionResults(attempt))
                .overallAdvice(attempt.getAdviceText())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public String getAttemptOwnerId(
            String attemptId) {

        return quizRepository.findAttemptById(attemptId)
                .map(Attempt::getUserId)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy attempt",
                        HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizResultResponse> getUserResults(
            String userId) {

        List<Attempt> attempts = quizRepository.findAttemptsByUserId(
                userId);
        List<String> attemptIds = attempts.stream().map(Attempt::getAttemptId).toList();
        Map<String, Long> responseCountByAttempt = quizRepository.countResponsesByAttemptIds(attemptIds);
        List<String> quizIds = attempts.stream().map(Attempt::getQuizId).distinct().toList();
        List<Quiz> loadedQuizzes = quizRepository.findQuizzesByIds(quizIds);
        if (loadedQuizzes == null) loadedQuizzes = quizIds.stream().map(quizRepository::findQuizById)
                .flatMap(Optional::stream).toList();
        Map<String, Quiz> quizById = loadedQuizzes.stream()
                .collect(Collectors.toMap(Quiz::getQuizId, quiz -> quiz));
        List<String> subjectIds = quizById.values().stream().map(Quiz::getSubjectId)
                .filter(java.util.Objects::nonNull).distinct().toList();
        List<Subject> loadedSubjects = contentRepository.findSubjectsByIds(subjectIds);
        if (loadedSubjects == null) loadedSubjects = subjectIds.stream().map(contentRepository::findSubjectById)
                .flatMap(Optional::stream).toList();
        Map<String, Subject> subjectById = loadedSubjects.stream()
                .collect(Collectors.toMap(Subject::getSubjectId, subject -> subject));

        return attempts.stream().filter(attempt -> {
                    long responseCount = responseCountByAttempt.getOrDefault(attempt.getAttemptId(), 0L);
                    Quiz quiz = quizById.get(attempt.getQuizId());
                    return responseCount > 0 && (quiz == null || quiz.getType() != QuizType.ADAPTIVE
                            || responseCount >= attempt.getTotalQuestion());
                })
                .map(attempt -> toAttemptSummary(attempt, quizById, subjectById))
                .toList();
    }

    private QuizResultResponse toAttemptSummary(
            Attempt attempt, Map<String, Quiz> quizById, Map<String, Subject> subjectById) {
        Quiz quiz = quizById.get(attempt.getQuizId());
        Subject subject = quiz == null ? null : subjectById.get(quiz.getSubjectId());
        String resolvedQuizName = quiz != null && quiz.getName() != null && !quiz.getName().isBlank()
                ? quiz.getName()
                : "Bài kiểm tra";
        String resolvedSubjectName = subject != null && subject.getName() != null && !subject.getName().isBlank()
                ? subject.getName()
                : "Môn học";
        int durationMinutes = quiz == null ? 0 : quiz.getDuration();
        return QuizResultResponse.builder()
                .attemptId(attempt.getAttemptId())
                .quizId(attempt.getQuizId())
                .quizName(resolvedQuizName)
                .subjectName(resolvedSubjectName)
                .durationMinutes(durationMinutes)
                .submittedAt(attempt.getSubmittedAt() == null ? attempt.getCreatedAt() : attempt.getSubmittedAt())
                .rawScore(attempt.getRawScore())
                .totalQuestion(attempt.getTotalQuestion())
                .overallTheta(attempt.getOverallTheta())
                .skillResults(List.of())
                .questionResults(List.of())
                .build();
    }

    private List<QuestionResultResponse> buildQuestionResults(
            Attempt attempt) {

        List<Response> responses = quizRepository.findResponsesByAttemptId(
                attempt.getAttemptId());
        Map<String, Response> responseByQuestionId = responses.stream()
                .collect(Collectors.toMap(
                        Response::getQuestionId,
                        response -> response,
                        (first, ignored) -> first));
        List<QuizQuestion> quizQuestions = quizRepository.findQuizQuestionsByQuizId(
                attempt.getQuizId());

        // Batch load câu hỏi + option để tránh N+1 trong vòng lặp.
        List<String> orderedQuestionIds = quizQuestions.stream()
                .map(QuizQuestion::getQuestionId)
                .toList();

        Map<String, Question> questionById = quizRepository
                .findQuestionsByIds(orderedQuestionIds)
                .stream()
                .collect(Collectors.toMap(
                        Question::getQuestionId,
                        q -> q,
                        (first, ignored) -> first));

        Map<String, List<Option>> optionsByQuestionId = quizRepository
                .findOptionsByQuestionIds(orderedQuestionIds)
                .stream()
                .collect(Collectors.groupingBy(Option::getQuestionId));

        List<QuestionResultResponse> questionResults = new ArrayList<>();

        for (int index = 0; index < quizQuestions.size(); index++) {
            QuizQuestion quizQuestion = quizQuestions.get(index);
            String questionId = quizQuestion.getQuestionId();
            Question question = questionById.get(questionId);

            if (question == null) {
                continue;
            }

            List<Option> options = optionsByQuestionId
                    .getOrDefault(questionId, List.of());
            String correctOptionId = options.stream()

                    .filter(Option::isCorrected)
                    .map(Option::getOptionId)
                    .findFirst()
                    .orElse(null);
            Response response = responseByQuestionId.get(questionId);

            questionResults.add(QuestionResultResponse.builder()
                    .questionId(question.getQuestionId())
                    .questionNumber(index + 1)
                    .content(question.getContent())
                    .explanation(question.getExplanation())
                    .contentFormat(question.getContentFormat())
                    .selectedOptionId(response == null ? null : response.getOptionId())
                    .correctOptionId(correctOptionId)
                    .correct(response != null && response.isScore())
                    .options(options.stream()
                            .map(option -> OptionResponse.builder()
                                    .optionId(option.getOptionId())
                                    .content(option.getContent())
                                    .contentFormat(question.getContentFormat())
                                    .corrected(option.isCorrected())
                                    .build())
                            .toList())
                    .build());
        }

        return questionResults;
    }
}

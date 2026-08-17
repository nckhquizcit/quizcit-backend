package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
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
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.QuestionResultResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.QuizResultResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.skill.SkillResultResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.skill.SkillThetaData;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.StartQuizResponse;
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
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AttemptServiceImplement
        implements AttemptService {

    private static final double NEW_THETA_WEIGHT = 0.3;
    private static final double EXISTING_THETA_WEIGHT = 0.7;

    private final QuizRepository quizRepository;
    private final ContentRepository contentRepository;
    private final AttemptMapper mapper;
    private final ProgressRepository progressRepository;
    private final SkillRepository skillRepository;

    private final AIService aiService;
    private final AdaptiveQuizService adaptiveQuizService;
    private final SubjectVisibilityGuard visibilityGuard;

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
                new SubjectVisibilityGuard(contentRepository, quizRepository));
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
                questions.size());

        quizRepository.save(attempt);

        return mapper.toStartQuizResponse(attempt);
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

        Map<String, String> submittedAnswers = new HashMap<>();

        for (SubmitAnswerRequest answer : request.getAnswers()) {
            if (!quizQuestionIds.contains(answer.getQuestionId())) {
                throw new AppException(
                        "Câu trả lời không thuộc đề thi đang nộp",
                        HttpStatus.BAD_REQUEST);
            }

            submittedAnswers.putIfAbsent(
                    answer.getQuestionId(),
                    answer.getOptionId());
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

        // Ước lượng theta cho TẤT CẢ skill trong 1 lần gọi AI (batch) thay vì
        // gọi lặp từng skill → loại bỏ N lần round-trip HTTP (rất nặng khi
        // AI Service cold start). Fallback nội bộ được áp dụng tự động cho
        // các skill mà AI Service không trả về.
        Map<String, EstimateThetaRequest> thetaRequests = new LinkedHashMap<>();
        for (var entry : skillMap.entrySet()) {
            SkillThetaData data = entry.getValue();
            thetaRequests.put(
                    entry.getKey(),
                    new EstimateThetaRequest(
                            data.getResponses(),
                            data.getDifficulties(),
                            data.getWeights()));
        }

        Map<String, Double> estimatedThetaBySkill = aiService.estimateThetaBatch(thetaRequests);

        Map<String, Double> skillThetaMap = new HashMap<>();
        List<UserSkillAbility> loadedAbilities = progressRepository.findUserSkillAbilities(attempt.getUserId());
        if (loadedAbilities == null) {
            loadedAbilities = skillMap.keySet().stream().map(skillId -> progressRepository
                    .findUserSkillAbility(attempt.getUserId(), skillId).orElse(null))
                    .filter(java.util.Objects::nonNull).toList();
        }
        Map<String, UserSkillAbility> abilityBySkill = loadedAbilities.stream()
                .filter(ability -> skillMap.containsKey(ability.getSkillId()))
                .collect(Collectors.toMap(UserSkillAbility::getSkillId, ability -> ability));
        List<UserSkillAbility> abilitiesToSave = new ArrayList<>();
        List<AbilityHistory> historiesToSave = new ArrayList<>();

        for (var entry : skillMap.entrySet()) {

            String skillId = entry.getKey();

            double estimatedTheta = estimatedThetaBySkill.getOrDefault(skillId, 0.0);

            UserSkillAbility ability = abilityBySkill.get(skillId);

            double oldTheta = 0.0;

            if (ability == null) {

                ability = UserSkillAbility.create(
                        attempt.getUserId(),
                        skillId);

            } else {

                oldTheta = ability.getTheta() == null ? 0.0 : ability.getTheta();
            }

            double blendedTheta = blendTheta(
                    oldTheta,
                    estimatedTheta);

            skillThetaMap.put(
                    skillId,
                    blendedTheta);

            ability.updateAbility(
                    blendedTheta);

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
        double overallTheta = skillThetaMap.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        double rawScore = correctCount;
        attempt.complete(
                rawScore,
                overallTheta);

        // MỚI: sinh lời khuyên cải thiện dựa trên delta theta từng skill vừa tính,
        // lưu 1 lần tại thời điểm nộp bài (không tính lại mỗi lần xem kết quả).
        String adviceText = aiService.generateAdvice(buildSkillDeltaItems(historiesToSave));
        attempt.attachAdvice(adviceText);

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
        List<Skill> skills = skillRepository.findByIds(skillIds);
        if (skills == null) {
            skills = skillIds.stream().map(skillRepository::findById)
                    .flatMap(Optional::stream).toList();
        }
        Map<String, String> skillNames = skills.stream()
                .collect(Collectors.toMap(Skill::getSkillId, Skill::getName));

        return historiesToSave.stream()
                .map(history -> new SkillDeltaItem(
                        skillNames.getOrDefault(history.getSkillId(), history.getSkillId()),
                        history.getTheta() - history.getDelta(),
                        history.getTheta(),
                        history.getDelta()))
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

        new SubjectVisibilityGuard(contentRepository, quizRepository)
                .requireVisibleQuiz(attempt.getQuizId());

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

        List<Skill> loadedSkills = skillRepository.findByIds(latestHistoryBySkill.keySet());
        if (loadedSkills == null) loadedSkills = latestHistoryBySkill.keySet().stream()
                .map(skillRepository::findById).flatMap(Optional::stream).toList();
        Map<String, String> skillNames = loadedSkills.stream()
                .collect(Collectors.toMap(Skill::getSkillId, Skill::getName));
        List<SkillResultResponse> skillResults = latestHistoryBySkill.values().stream()
                .map(history -> SkillResultResponse.builder()
                        .skillId(history.getSkillId())
                        .skillName(skillNames.get(history.getSkillId()))
                        .theta(history.getTheta())
                        .delta(history.getDelta())
                        .build())
                .toList();
        Quiz quiz = quizRepository.findQuizById(attempt.getQuizId())
                .orElse(null);
        Subject subject = quiz == null
                ? null
                : contentRepository.findSubjectById(quiz.getSubjectId())
                        .orElse(null);

        return QuizResultResponse.builder()
                .attemptId(attempt.getAttemptId())
                .quizId(attempt.getQuizId())
                .quizName(quiz == null ? null : quiz.getName())
                .subjectName(subject == null ? null : subject.getName())
                .submittedAt(attempt.getCreatedAt())
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
        List<Response> loadedResponses = quizRepository.findResponsesByAttemptIds(attemptIds);
        if (loadedResponses == null) loadedResponses = attemptIds.stream()
                .flatMap(id -> quizRepository.findResponsesByAttemptId(id).stream()).toList();
        Map<String, Long> responseCountByAttempt = loadedResponses
                .stream().collect(Collectors.groupingBy(Response::getAttemptId, Collectors.counting()));
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
        return QuizResultResponse.builder()
                .attemptId(attempt.getAttemptId())
                .quizId(attempt.getQuizId())
                .quizName(quiz == null ? null : quiz.getName())
                .subjectName(subject == null ? null : subject.getName())
                .submittedAt(attempt.getCreatedAt())
                .rawScore(attempt.getRawScore())
                .totalQuestion(attempt.getTotalQuestion())
                .overallTheta(attempt.getOverallTheta())
                .skillResults(List.of())
                .questionResults(List.of())
                .build();
    }

    private double blendTheta(
            double oldTheta,
            double newTheta) {

        return newTheta * NEW_THETA_WEIGHT
                + oldTheta * EXISTING_THETA_WEIGHT;
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

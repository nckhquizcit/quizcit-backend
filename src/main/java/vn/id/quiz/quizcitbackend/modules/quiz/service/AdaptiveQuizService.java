package vn.id.quiz.quizcitbackend.modules.quiz.service;

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
import vn.id.quiz.quizcitbackend.modules.ai.service.AIService;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaUserUnlockedChapterRepository;
import vn.id.quiz.quizcitbackend.modules.content.service.SubjectVisibilityGuard;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.AbilityHistory;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.QuestionSkill;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.UserSkillAbility;
import vn.id.quiz.quizcitbackend.modules.progress.repository.ProgressRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.adaptive.AdaptiveQuizResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.option.OptionResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.QuestionResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.QuestionSkillResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.SubmitAnswerRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Attempt;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Option;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizQuestion;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizType;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Response;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence.JpaAttemptRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.SkillRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

@Service
@RequiredArgsConstructor
public class AdaptiveQuizService {
    private static final int PAGE_SIZE = 5;
    private static final int INITIAL_BUFFER_SIZE = 10;
    private static final double OLD_THETA_WEIGHT = 0.7;
    private static final double NEW_THETA_WEIGHT = 0.3;

    private final QuizRepository quizRepository;
    private final ContentRepository contentRepository;
    private final ProgressRepository progressRepository;
    private final SkillRepository skillRepository;
    private final JpaUserUnlockedChapterRepository unlockedChapterRepository;
    private final QuestionMapper questionMapper;
    private final AIService aiService;
    private final JpaAttemptRepository attemptRepository;

    @Transactional
    public AdaptiveQuizResponse start(String userId, String subjectId, int totalQuestion) {
        validateTotal(totalQuestion);
        new SubjectVisibilityGuard(contentRepository, quizRepository).requireVisibleSubject(subjectId);

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
        return state(attempt);
    }

    @Transactional(readOnly = true)
    public AdaptiveQuizResponse getState(String userId, String attemptId) {
        return state(requireAttempt(userId, attemptId));
    }

    @Transactional
    public AdaptiveQuizResponse submitBatch(String userId, String attemptId, List<SubmitAnswerRequest> answers) {
        Attempt locked = attemptRepository.findByAttemptIdForUpdate(attemptId)
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
        if (unansweredBuffered < PAGE_SIZE && assigned.size() < attempt.getTotalQuestion()) {
            appendQuestions(attempt, unlockedChapters(userId,
                    quizRepository.findQuizById(attempt.getQuizId()).orElseThrow().getSubjectId()), PAGE_SIZE);
        }
        return state(attempt);
    }

    private void validateTotal(int total) {
        if (total < 40 || total % PAGE_SIZE != 0) {
            throw new AppException("Số câu phải từ 40 trở lên và chia hết cho 5", HttpStatus.BAD_REQUEST);
        }
    }

    private List<Chapter> unlockedChapters(String userId, String subjectId) {
        List<Chapter> chapters = contentRepository.findAllChapter(subjectId);
        Set<String> unlockedIds = unlockedChapterRepository
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
        List<Question> selected = eligibleQuestions(attempt.getUserId(), chapters, excluded).stream()
                .limit(Math.min(requested, attempt.getTotalQuestion() - current.size())).toList();
        if (selected.size() < Math.min(requested, attempt.getTotalQuestion() - current.size())) {
            throw new AppException("Không còn đủ câu hỏi phù hợp cho bài thích ứng", HttpStatus.CONFLICT);
        }
        int index = current.size() + 1;
        for (Question question : selected) {
            quizRepository.save(QuizQuestion.create(attempt.getQuizId(), question.getQuestionId(), index++));
        }
    }

    private List<Question> eligibleQuestions(String userId, List<Chapter> chapters, Set<String> excluded) {
        Map<String, Double> theta = progressRepository.findUserSkillAbilities(userId).stream()
                .collect(Collectors.toMap(UserSkillAbility::getSkillId,
                        ability -> ability.getTheta() == null ? 0.0 : ability.getTheta(), (a, b) -> a));
        if (theta.isEmpty()) return List.of();
        List<Question> pool = chapters.stream().flatMap(chapter ->
                quizRepository.findQuestionsByChapterId(chapter.getChapterId()).stream())
                .filter(question -> !excluded.contains(question.getQuestionId())).collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(pool); // trộn trước mỗi lần chọn để tie không thiên vị thứ tự DB
        Map<String, List<QuestionSkill>> mappings = progressRepository
                .findQuestionSkillsByQuestionIds(pool.stream().map(Question::getQuestionId).toList()).stream()
                .filter(mapping -> theta.containsKey(mapping.getSkillId()))
                .collect(Collectors.groupingBy(QuestionSkill::getQuestionId));
        return pool.stream().filter(question -> mappings.containsKey(question.getQuestionId()))
                .sorted(Comparator.comparing((Question question) -> mappings.get(question.getQuestionId()).stream()
                                .noneMatch(QuestionSkill::isPrimary))
                        .thenComparingDouble(question -> mappings.get(question.getQuestionId()).stream()
                                .mapToDouble(mapping -> Math.abs(theta.get(mapping.getSkillId()) - question.getDifficultyB()))
                                .min().orElse(Double.MAX_VALUE)))
                .toList();
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

    private void updateTheta(Attempt attempt, Map<String, Question> questions, List<Response> responses) {
        Map<String, Response> responseMap = responses.stream().collect(Collectors.toMap(Response::getQuestionId, r -> r));
        Map<String, List<QuestionSkill>> mappings = progressRepository
                .findQuestionSkillsByQuestionIds(questions.keySet()).stream().collect(Collectors.groupingBy(QuestionSkill::getSkillId));
        Map<String, EstimateThetaRequest> requests = new LinkedHashMap<>();
        mappings.forEach((skillId, values) -> requests.put(skillId, new EstimateThetaRequest(
                values.stream().map(value -> responseMap.get(value.getQuestionId()).isScore() ? 1 : 0).toList(),
                values.stream().map(value -> questions.get(value.getQuestionId()).getDifficultyB()).toList(),
                values.stream().map(QuestionSkill::getWeight).toList())));
        Map<String, Double> estimated = aiService.estimateThetaBatch(requests);
        estimated.forEach((skillId, newTheta) -> {
            UserSkillAbility ability = progressRepository.findUserSkillAbility(attempt.getUserId(), skillId)
                    .orElseGet(() -> UserSkillAbility.create(attempt.getUserId(), skillId));
            double old = ability.getTheta() == null ? 0 : ability.getTheta();
            ability.updateAbility(old * OLD_THETA_WEIGHT + newTheta * NEW_THETA_WEIGHT);
            progressRepository.save(ability);
        });
    }

    private void complete(Attempt attempt) {
        List<Response> responses = quizRepository.findResponsesByAttemptId(attempt.getAttemptId());
        Set<String> questionIds = responses.stream().map(Response::getQuestionId).collect(Collectors.toSet());
        Set<String> skillIds = progressRepository.findQuestionSkillsByQuestionIds(questionIds).stream()
                .map(QuestionSkill::getSkillId).collect(Collectors.toSet());
        List<Double> theta = new ArrayList<>();
        for (String skillId : skillIds) {
            progressRepository.findUserSkillAbility(attempt.getUserId(), skillId).ifPresent(ability -> {
                double value = ability.getTheta() == null ? 0 : ability.getTheta();
                theta.add(value);
                progressRepository.save(AbilityHistory.create(attempt.getUserId(), skillId, value, 0, attempt.getAttemptId()));
            });
        }
        attempt.complete(responses.stream().filter(Response::isScore).count(),
                theta.stream().mapToDouble(Double::doubleValue).average().orElse(0));
        quizRepository.save(attempt);
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
        return attempt;
    }

    private AdaptiveQuizResponse state(Attempt attempt) {
        List<Response> responses = quizRepository.findResponsesByAttemptId(attempt.getAttemptId());
        Set<String> answered = responses.stream().map(Response::getQuestionId).collect(Collectors.toSet());
        List<String> nextIds = quizRepository.findQuizQuestionsByQuizId(attempt.getQuizId()).stream()
                .map(QuizQuestion::getQuestionId).filter(id -> !answered.contains(id)).limit(PAGE_SIZE).toList();
        return AdaptiveQuizResponse.builder().quizId(attempt.getQuizId()).attemptId(attempt.getAttemptId())
                .totalQuestion(attempt.getTotalQuestion()).answeredQuestion(responses.size())
                .page(responses.size() / PAGE_SIZE + 1).completed(responses.size() >= attempt.getTotalQuestion())
                .questions(toQuestionResponses(nextIds)).build();
    }

    private List<QuestionResponse> toQuestionResponses(Collection<String> ids) {
        Map<String, Question> questions = quizRepository.findQuestionsByIds(ids).stream()
                .collect(Collectors.toMap(Question::getQuestionId, q -> q));
        Map<String, List<Option>> options = quizRepository.findOptionsByQuestionIds(ids).stream()
                .collect(Collectors.groupingBy(Option::getQuestionId));
        Map<String, List<QuestionSkill>> mappings = progressRepository.findQuestionSkillsByQuestionIds(ids).stream()
                .collect(Collectors.groupingBy(QuestionSkill::getQuestionId));
        return ids.stream().map(id -> {
            QuestionResponse result = questionMapper.toResponse(questions.get(id));
            result.setOptions(options.getOrDefault(id, List.of()).stream().map(option -> OptionResponse.builder()
                    .optionId(option.getOptionId()).content(option.getContent()).corrected(false).build()).toList());
            result.setSkills(mappings.getOrDefault(id, List.of()).stream().map(mapping -> QuestionSkillResponse.builder()
                    .skillId(mapping.getSkillId()).name(skillRepository.findById(mapping.getSkillId()).map(s -> s.getName()).orElse(null))
                    .weight(mapping.getWeight()).primary(mapping.isPrimary()).build()).toList());
            return result;
        }).toList();
    }
}

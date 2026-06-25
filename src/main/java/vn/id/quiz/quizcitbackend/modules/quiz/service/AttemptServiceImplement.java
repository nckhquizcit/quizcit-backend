package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.util.ArrayList;
import java.util.HashMap;
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
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.AbilityHistory;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.QuestionSkill;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.UserSkillAbility;
import vn.id.quiz.quizcitbackend.modules.progress.repository.ProgressRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.QuizResultResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.SkillResultResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.SkillThetaData;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.StartQuizResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.SubmitAnswerRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.SubmitQuizRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Attempt;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Option;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizQuestion;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Response;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

@Service
@RequiredArgsConstructor
public class AttemptServiceImplement
        implements AttemptService {

    private final QuizRepository quizRepository;
    private final AttemptMapper mapper;
    private final ProgressRepository progressRepository;

    private final AIService aiService;

    @Override
    @Transactional
    public StartQuizResponse startQuiz(
            String userId,
            String quizId) {

        Quiz quiz = quizRepository.findQuizById(quizId)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy đề thi",
                        HttpStatus.NOT_FOUND));

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
        Attempt attempt = quizRepository
                .findAttemptById(
                        request.getAttemptId())
                .orElseThrow(
                        () -> new AppException(
                                "Không tìm thấy attempt",
                                HttpStatus.NOT_FOUND));

        if (request.getQuizId() != null && !request.getQuizId().isBlank()
                && !attempt.getQuizId().equals(request.getQuizId())) {
            throw new AppException(
                    "Attempt không thuộc đề thi đang nộp",
                    HttpStatus.BAD_REQUEST);
        }

        if (!quizRepository.findResponsesByAttemptId(attempt.getAttemptId()).isEmpty()) {
            return getResult(attempt.getAttemptId());
        }

        List<QuizQuestion> quizQuestions = quizRepository.findQuizQuestionsByQuizId(
                attempt.getQuizId());

        Set<String> quizQuestionIds = quizQuestions.stream()
                .map(QuizQuestion::getQuestionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

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

        Map<String, SkillThetaData> skillMap = new HashMap<>();
        int correctCount = 0;

        for (String questionId : quizQuestionIds) {
            Question question = quizRepository
                    .findQuestionById(
                            questionId)
                    .orElseThrow(() -> new AppException(
                            "Không tìm thấy câu hỏi trong đề thi",
                            HttpStatus.NOT_FOUND));

            String optionId = submittedAnswers.get(questionId);
            Option option = null;
            boolean correct = false;

            if (optionId != null && !optionId.isBlank()) {
                option = quizRepository
                        .findOptionById(
                                optionId)
                        .orElseThrow(() -> new AppException(
                                "Không tìm thấy đáp án đã chọn",
                                HttpStatus.NOT_FOUND));

                if (!questionId.equals(option.getQuestionId())) {
                    throw new AppException(
                            "Đáp án đã chọn không thuộc câu hỏi trong đề thi",
                            HttpStatus.BAD_REQUEST);
                }

                correct = option.isCorrected();
            }

            Response response = Response.create(
                    attempt.getAttemptId(),
                    question.getQuestionId(),
                    option == null ? null : option.getOptionId(),
                    correct);
            quizRepository.save(response);

            if (correct) {
                correctCount++;
            }

            List<QuestionSkill> mappings = progressRepository
                    .findQuestionSkillsByQuestionId(
                            question.getQuestionId());

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
        Map<String, Double> skillThetaMap = new HashMap<>();

        for (var entry : skillMap.entrySet()) {

            String skillId = entry.getKey();

            SkillThetaData data = entry.getValue();

            double theta = aiService.estimateTheta(
                    new EstimateThetaRequest(
                            data.getResponses(),
                            data.getDifficulties(),
                            data.getWeights()))
                    .theta();

            skillThetaMap.put(
                    skillId,
                    theta);

            UserSkillAbility ability = progressRepository
                    .findUserSkillAbility(
                            attempt.getUserId(),
                            skillId)
                    .orElse(null);

            double oldTheta = 0.0;

            if (ability == null) {

                ability = UserSkillAbility.create(
                        attempt.getUserId(),
                        skillId);

            } else {

                oldTheta = ability.getTheta();
            }

            // TODO: standardError tạm để 0.0; sau này lấy từ EstimateThetaResponse.standardError
            // khi AI Service trả về sai số chuẩn của ước lượng theta.
            ability.updateAbility(
                    theta,
                    0.0);

            progressRepository.save(
                    ability);

            AbilityHistory history = AbilityHistory.create(
                    attempt.getUserId(),
                    skillId,
                    theta,
                    theta - oldTheta,
                    // TODO: standardError tạm để 0.0; thay bằng giá trị thật từ AI khi có.
                    0.0,
                    attempt.getAttemptId());


            progressRepository.save(
                    history);
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

        quizRepository.save(
                attempt);
        return getResult(attempt.getAttemptId());
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

        List<AbilityHistory> histories = progressRepository.findAbilityHistoriesByAttemptId(
                attemptId);

        List<SkillResultResponse> skillResults = histories.stream()
                .map(history -> SkillResultResponse.builder()
                        .skillId(history.getSkillId())
                        .theta(history.getTheta())
                        .standardError(
                                history.getStandardError())
                        .build())
                .toList();

        return QuizResultResponse.builder()
                .attemptId(attempt.getAttemptId())
                .quizId(attempt.getQuizId())
                .rawScore(attempt.getRawScore())
                .totalQuestion(attempt.getTotalQuestion())
                .overallTheta(attempt.getOverallTheta())
                .skillResults(skillResults)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public String getAttemptOwnerId(
            String attemptId) {

        Attempt attempt = quizRepository
                .findAttemptById(attemptId)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy kết quả bài thi",
                        HttpStatus.NOT_FOUND));

        return attempt.getUserId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizResultResponse> getUserResults(
            String userId) {

        List<Attempt> attempts = quizRepository.findAttemptsByUserId(
                userId);

        return mapper.toQuizResultResponseList(
                attempts);
    }
}

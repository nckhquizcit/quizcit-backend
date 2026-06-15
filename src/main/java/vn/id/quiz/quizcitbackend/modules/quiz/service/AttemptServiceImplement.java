package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaRequest;
import vn.id.quiz.quizcitbackend.modules.ai.dto.EstimateThetaResponse;
import vn.id.quiz.quizcitbackend.modules.ai.service.AIService;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.AbilityHistory;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.QuestionSkill;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.UserSkillAbility;
import vn.id.quiz.quizcitbackend.modules.progress.repository.ProgressRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.QuizResultResponse;
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
                questions.size(),
                0.0);

        quizRepository.save(attempt);

        return mapper.toStartQuizResponse(attempt);
    }

    @Override
    @Transactional
    public QuizResultResponse submitQuiz(
            SubmitQuizRequest request) {
        // Kiếm lần thử để tạo bài nộp
        Attempt attempt = quizRepository
                .findAttemptById(
                        request.getAttemptId())
                .orElseThrow(
                        () -> new AppException(
                                "Không tìm thấy attempt",
                                HttpStatus.NOT_FOUND));
        // Tạo danh sách các skill có trong bài
        Map<String, SkillThetaData> skillMap = new HashMap<>();

        // Duyệt đáp án
        int correctCount = 0;

        for (SubmitAnswerRequest answer : request.getAnswers()) {
            Question question = quizRepository
                    .findQuestionById(
                            answer.getQuestionId())
                    .orElseThrow();
            Option option = quizRepository
                    .findOptionById(
                            answer.getOptionId())
                    .orElseThrow();
            boolean correct = option.isCorrected();
            Response response = Response.create(
                    attempt.getAttemptId(),
                    question.getQuestionId(),
                    option.getOptionId(),
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
                        .add(1.0);
            }
        }
        Map<String, Double> skillThetaMap = new HashMap<>();

        for (var entry : skillMap.entrySet()) {

            String skillId = entry.getKey();

            SkillThetaData data = entry.getValue();

            double theta = aiService.estimateTheta(
                    data.getResponses(),
                    data.getDifficulties(),
                    data.getWeights());

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
                    0.0,
                    attempt.getAttemptId());

            progressRepository.save(
                    history);
        }
        double thetaAfter = skillThetaMap.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        double rawScore = (double) correctCount
                / attempt.getTotalQuestion();
        attempt.complete(
                rawScore,
                thetaAfter,
                thetaAfter,
                0.0);

        quizRepository.save(
                attempt);
        return mapper.toQuizResultResponse(
                attempt);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizResultResponse getResult(
            String attemptId) {

        throw new UnsupportedOperationException(
                "Not implemented yet");
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizResultResponse> getUserResults(
            String userId) {

        throw new UnsupportedOperationException(
                "Not implemented yet");
    }
}
package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.QuestionSkill;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.Skill;
import vn.id.quiz.quizcitbackend.modules.progress.repository.SkillRepository;

import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.option.CreateOptionRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.CreateQuestionRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.option.OptionResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.QuestionResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.QuestionSkillMappingRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.QuestionSkillResponse;

import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.UpdateQuestionRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Option;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

@Service
@RequiredArgsConstructor
public class QuestionServiceImplement implements QuestionService {

    private final QuizRepository quizRepository;
    private final ContentRepository contentRepository;
    private final QuestionMapper mapper;
    private final SkillRepository skillRepository;


    @Override
    @Transactional
    public QuestionResponse createQuestion(
            CreateQuestionRequest request) {

        validatePrimarySkill(request.getSkills());

        contentRepository.findSubjectById(request.getSubjectId())
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy môn học",
                        HttpStatus.NOT_FOUND));

        contentRepository.findChapterById(request.getChapterId())
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy chương học",
                        HttpStatus.NOT_FOUND));

        Question question = Question.create(
                request.getSubjectId(),
                request.getChapterId(),
                request.getContent(),
                request.getExplanation(),
                request.getDifficultyB());

        quizRepository.save(question);

        List<OptionResponse> optionResponses = createOptionsForQuestion(
                question.getQuestionId(),
                request.getOptions());

        // Phase 2 (P1): lưu skill mapping nếu có
        if (request.getSkills() != null) {
            for (QuestionSkillMappingRequest s : request.getSkills()) {
                quizRepository.saveQuestionSkill(
                        QuestionSkill.create(
                                question.getQuestionId(),
                                s.getSkillId(),
                                s.getWeight(),
                                s.isPrimary()));
            }
        }

        QuestionResponse response = mapper.toResponse(question);
        response.setOptions(optionResponses);
        response.setCorrectOptionId(extractCorrectOptionId(optionResponses));
        response.setSkills(toSkillResponses(quizRepository.findQuestionSkillsByQuestionId(question.getQuestionId())));
        return response;
    }

    @Override
    @Transactional
    public void updateQuestion(
            String questionId,
            UpdateQuestionRequest request) {

        validatePrimarySkill(request.getSkills());

        Question question = quizRepository.findQuestionById(
                questionId)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy câu hỏi",
                        HttpStatus.NOT_FOUND));

        question.setContent(request.getContent());
        question.setExplanation(request.getExplanation());
        question.setDifficultyB(request.getDifficultyB());
        quizRepository.save(question);

        // Phase 2 (P1): replace options nếu request chỉ định
        if (request.getOptions() != null) {
            quizRepository.deleteOptionsByQuestionId(questionId);
            createOptionsForQuestion(questionId, request.getOptions());
        }

        // Phase 2 (P1): replace skill mapping nếu request chỉ định
        if (request.getSkills() != null) {
            quizRepository.deleteQuestionSkillsByQuestionId(questionId);
            for (QuestionSkillMappingRequest s : request.getSkills()) {
                quizRepository.saveQuestionSkill(
                        QuestionSkill.create(
                                questionId,
                                s.getSkillId(),
                                s.getWeight(),
                                s.isPrimary()));
            }
        }
    }

    @Override
    @Transactional
    public void deleteQuestion(String questionId) {
        quizRepository.deleteResponsesByQuestionId(questionId);
        quizRepository.deleteQuizQuestionsByQuestionId(questionId);
        quizRepository.deleteQuestionSkillsByQuestionId(questionId);
        quizRepository.deleteOptionsByQuestionId(questionId);
        quizRepository.deleteQuestionById(questionId);
    }

    @Override
    public QuestionResponse getQuestionById(String questionId) {
        Question question = quizRepository.findQuestionById(questionId)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy câu hỏi",
                        HttpStatus.NOT_FOUND));
        List<Option> options = quizRepository.findOptionsByQuestionId(questionId);
        List<OptionResponse> optionResponses = mapper.toOptionResponseList(options);

        QuestionResponse response = mapper.toResponse(question);
        response.setOptions(optionResponses);
        response.setCorrectOptionId(extractCorrectOptionId(optionResponses));
        response.setSkills(toSkillResponses(quizRepository.findQuestionSkillsByQuestionId(questionId)));
        return response;
    }

    @Override
    public List<QuestionResponse> getQuestionsByChapter(
            String chapterId) {
        List<Question> questions = quizRepository.findQuestionsByChapterId(chapterId);
        return questions.stream()
                .map(q -> {
                    QuestionResponse r = mapper.toResponse(q);
                    r.setOptions(Collections.emptyList());
                    r.setSkills(toSkillResponses(quizRepository.findQuestionSkillsByQuestionId(q.getQuestionId())));
                    return r;
                })
                .toList();
    }

    /**
     * Phase 2 (P0): Lấy danh sách câu hỏi trong một quiz kèm theo option
     * để frontend làm bài trắc nghiệm.
     */
    public List<QuestionResponse> getQuestionsByQuiz(String quizId) {
        List<Question> questions = quizRepository.findQuestionsByQuizId(quizId);
        return questions.stream()
                .map(q -> {
                    List<Option> options = quizRepository.findOptionsByQuestionId(
                            q.getQuestionId());
                    List<OptionResponse> optionResponses = mapper
                            .toOptionResponseList(options)
                            .stream()
                            // Khi làm bài, ẩn cờ corrected khỏi response để
                            // tránh lộ đáp án.
                            .peek(o -> o.setCorrected(false))
                            .toList();
                    QuestionResponse r = mapper.toResponse(q);
                    r.setOptions(optionResponses);
                    // Task 1: map skill (kèm tên) để FE hiển thị popup skill.
                    r.setSkills(toSkillResponses(
                            quizRepository.findQuestionSkillsByQuestionId(
                                    q.getQuestionId())));
                    return r;
                })
                .toList();
    }

    private List<OptionResponse> createOptionsForQuestion(

            String questionId,
            List<CreateOptionRequest> optionRequests) {

        if (optionRequests == null) {
            return Collections.emptyList();
        }

        return optionRequests.stream()
                .map(opt -> {
                    Option option = Option.create(
                            questionId,
                            opt.getContent(),
                            opt.isCorrected());
                    quizRepository.save(option);
                    return mapper.toOptionResponse(option);
                })
                .toList();
    }

    private String extractCorrectOptionId(List<OptionResponse> options) {
        if (options == null) {
            return null;
        }
        Optional<OptionResponse> correct = options.stream()
                .filter(OptionResponse::isCorrected)
                .findFirst();
        return correct.map(OptionResponse::getOptionId).orElse(null);
    }

    private List<QuestionSkillResponse> toSkillResponses(List<QuestionSkill> questionSkills) {
        if (questionSkills == null) {
            return Collections.emptyList();
        }

        return questionSkills.stream()
                .map(questionSkill -> {
                    // Task 1: enrich tên skill từ SkillRepository (nếu còn tồn tại).
                    String skillName = skillRepository
                            .findById(questionSkill.getSkillId())
                            .map(Skill::getName)
                            .orElse(null);
                    return QuestionSkillResponse.builder()
                            .skillId(questionSkill.getSkillId())
                            .name(skillName)
                            .weight(questionSkill.getWeight())
                            .primary(questionSkill.isPrimary())
                            .build();
                })
                .toList();
    }


    private void validatePrimarySkill(List<QuestionSkillMappingRequest> skills) {
        if (skills == null || skills.isEmpty()) {
            return;
        }

        long primaryCount = skills.stream()
                .filter(QuestionSkillMappingRequest::isPrimary)
                .count();

        if (primaryCount != 1) {
            throw new AppException(
                    "Câu hỏi có kỹ năng phải có đúng một kỹ năng chính",
                    HttpStatus.BAD_REQUEST);
        }
    }
}

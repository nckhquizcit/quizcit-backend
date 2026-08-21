package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;

import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaChapterSkillRepository;
import vn.id.quiz.quizcitbackend.modules.content.service.ContentConstants;
import vn.id.quiz.quizcitbackend.modules.content.service.SubjectVisibilityGuard;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.QuestionSkill;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.Skill;
import vn.id.quiz.quizcitbackend.modules.progress.repository.SkillRepository;

import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.option.CreateOptionRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.CreateQuestionRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.option.OptionResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.QuestionResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.QuestionSkillMappingRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.QuestionSkillResponse;

import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.AssignQuestionPlacementRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.BulkAssignQuestionPlacementRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question.UpdateQuestionRequest;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Option;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizType;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

@Service
public class QuestionServiceImplement implements QuestionService {
    private static final double WEIGHT_SUM_TOLERANCE = 0.000001;

    private final QuizRepository quizRepository;
    private final ContentRepository contentRepository;
    private final QuestionMapper mapper;
    private final SkillRepository skillRepository;
    private final JpaChapterSkillRepository chapterSkillRepository;
    private final SubjectVisibilityGuard visibilityGuard;

    @Autowired
    public QuestionServiceImplement(
            QuizRepository quizRepository,
            ContentRepository contentRepository,
            QuestionMapper mapper,
            SkillRepository skillRepository,
            JpaChapterSkillRepository chapterSkillRepository,
            SubjectVisibilityGuard visibilityGuard) {
        this.quizRepository = quizRepository;
        this.contentRepository = contentRepository;
        this.mapper = mapper;
        this.skillRepository = skillRepository;
        this.chapterSkillRepository = chapterSkillRepository;
        this.visibilityGuard = visibilityGuard;
    }

    QuestionServiceImplement(
            QuizRepository quizRepository,
            ContentRepository contentRepository,
            QuestionMapper mapper,
            SkillRepository skillRepository,
            JpaChapterSkillRepository chapterSkillRepository) {
        this(quizRepository, contentRepository, mapper, skillRepository, chapterSkillRepository,
                new SubjectVisibilityGuard(contentRepository, quizRepository));
    }


    @Override
    @Transactional
    public QuestionResponse createQuestion(
            CreateQuestionRequest request) {

        String subjectId = request.getSubjectId();
        String chapterId = request.getChapterId();

        if (isQuestionBankPlacement(subjectId, chapterId)) {
            Subject bankSubject = contentRepository.getOrCreateQuestionBankSubject();
            Chapter bankChapter = contentRepository.getOrCreateQuestionBankChapter(bankSubject.getSubjectId());
            subjectId = bankSubject.getSubjectId();
            chapterId = bankChapter.getChapterId();
        } else {
            validateQuestionPlacement(subjectId, chapterId);
        }

        validateOptions(request.getOptions());
        validateSkillMappings(request.getSkills(), chapterId);

        Question question = Question.create(
                subjectId,
                chapterId,
                request.getContent(),
                request.getExplanation(),
                request.getDifficultyB());
        question.setContentFormat(request.getContentFormat());

        quizRepository.save(question);

        List<OptionResponse> optionResponses = createOptionsForQuestion(
                question.getQuestionId(),
                request.getOptions(),
                question.getContentFormat());

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

        Question question = quizRepository.findQuestionById(
                questionId)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy câu hỏi",
                        HttpStatus.NOT_FOUND));

        String nextSubjectId = hasText(request.getSubjectId())
                ? request.getSubjectId()
                : question.getSubjectId();
        String nextChapterId = hasText(request.getChapterId())
                ? request.getChapterId()
                : question.getChapterId();

        if (isQuestionBankPlacement(nextSubjectId, nextChapterId)) {
            Subject bankSubject = contentRepository.getOrCreateQuestionBankSubject();
            Chapter bankChapter = contentRepository.getOrCreateQuestionBankChapter(bankSubject.getSubjectId());
            nextSubjectId = bankSubject.getSubjectId();
            nextChapterId = bankChapter.getChapterId();
        } else {
            validateQuestionPlacement(nextSubjectId, nextChapterId);
        }

        validateSkillMappings(request.getSkills(), nextChapterId);

        question.setSubjectId(nextSubjectId);
        question.setChapterId(nextChapterId);
        question.setContent(request.getContent());
        question.setExplanation(request.getExplanation());
        question.setContentFormat(request.getContentFormat());
        question.setDifficultyB(request.getDifficultyB());
        quizRepository.save(question);

        // Phase 2 (P1): replace options nếu request chỉ định
        if (request.getOptions() != null) {
            quizRepository.deleteOptionsByQuestionId(questionId);
            createOptionsForQuestion(questionId, request.getOptions(), question.getContentFormat());
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
    public QuestionResponse getQuestionById(
            String questionId,
            boolean includeCorrectAnswer) {
        Question question = quizRepository.findQuestionById(questionId)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy câu hỏi",
                        HttpStatus.NOT_FOUND));
        if (question.getSubjectId() != null) {
            visibilityGuard.requireAccessibleSubject(question.getSubjectId());
        } else if (!visibilityGuard.isAdministrator()) {
            throw new AppException(
                    "Không tìm thấy câu hỏi",
                    HttpStatus.NOT_FOUND);
        }
        List<Option> options = quizRepository.findOptionsByQuestionId(questionId);
        List<OptionResponse> optionResponses = mapper.toOptionResponseList(options);
        optionResponses.forEach(o -> o.setContentFormat(question.getContentFormat()));

        QuestionResponse response = mapper.toResponse(question);
        response.setOptions(optionResponses);
        response.setCorrectOptionId(extractCorrectOptionId(optionResponses));
        response.setSkills(toSkillResponses(quizRepository.findQuestionSkillsByQuestionId(questionId)));
        if (!includeCorrectAnswer) {
            hideCorrectAnswers(response);
        }
        return response;
    }

    @Override
    public List<QuestionResponse> getQuestionsByChapter(
            String chapterId) {
        visibilityGuard.requireVisibleChapter(chapterId);
        return toQuestionSummaries(quizRepository.findQuestionsByChapterId(chapterId));
    }

    @Override
    public List<QuestionResponse> getAllQuestions() {
        return toQuestionSummaries(quizRepository.findAllQuestions());
    }

    private List<QuestionResponse> toQuestionSummaries(List<Question> questions) {
        Map<String, List<QuestionSkill>> skillsByQuestion = quizRepository
                .findQuestionSkillsByQuestionIds(questions.stream().map(Question::getQuestionId).toList())
                .stream().collect(java.util.stream.Collectors.groupingBy(QuestionSkill::getQuestionId));
        Map<String, String> skillNames = loadSkillNames(skillsByQuestion.values().stream().flatMap(List::stream).toList());
        return questions.stream()
                .map(q -> {
                    QuestionResponse r = mapper.toResponse(q);
                    r.setOptions(Collections.emptyList());
                    r.setSkills(toSkillResponses(skillsByQuestion.getOrDefault(q.getQuestionId(), List.of()), skillNames));
                    return r;
                })
                .toList();
    }

    /**
     * Phase 2 (P0): Lấy danh sách câu hỏi trong một quiz kèm theo option
     * để frontend làm bài trắc nghiệm.
     */
    public List<QuestionResponse> getQuestionsByQuiz(String quizId) {
        visibilityGuard.requireVisibleQuiz(quizId);
        quizRepository.findQuizById(quizId).ifPresent(quiz -> {
            if (quiz.getType() == QuizType.ADAPTIVE) {
                throw new AppException(
                        "Câu hỏi thích ứng chỉ được tải theo batch của attempt",
                        HttpStatus.BAD_REQUEST);
            }
        });
        List<Question> questions = quizRepository.findQuestionsByQuizId(quizId);
        List<String> questionIds = questions.stream().map(Question::getQuestionId).toList();
        Map<String, List<Option>> optionsByQuestion = quizRepository.findOptionsByQuestionIds(questionIds)
                .stream().collect(java.util.stream.Collectors.groupingBy(Option::getQuestionId));
        Map<String, List<QuestionSkill>> skillsByQuestion = quizRepository.findQuestionSkillsByQuestionIds(questionIds)
                .stream().collect(java.util.stream.Collectors.groupingBy(QuestionSkill::getQuestionId));
        Map<String, String> skillNames = loadSkillNames(skillsByQuestion.values().stream().flatMap(List::stream).toList());
        return questions.stream()
                .map(q -> {
                    List<Option> options = optionsByQuestion.getOrDefault(q.getQuestionId(), List.of());
                    List<OptionResponse> optionResponses = mapper
                            .toOptionResponseList(options)
                            .stream()
                            // Khi làm bài, ẩn cờ corrected khỏi response để
                            // tránh lộ đáp án.
                            .peek(o -> o.setCorrected(false))
                            .peek(o -> o.setContentFormat(q.getContentFormat()))
                            .toList();
                    QuestionResponse r = mapper.toResponse(q);
                    r.setOptions(optionResponses);
                    // Task 1: map skill (kèm tên) để FE hiển thị popup skill.
                    r.setSkills(toSkillResponses(
                            skillsByQuestion.getOrDefault(q.getQuestionId(), List.of()), skillNames));
                    return r;
                })
                .toList();
    }

    private List<OptionResponse> createOptionsForQuestion(

            String questionId,
            List<CreateOptionRequest> optionRequests,
            String contentFormat) {

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
                    OptionResponse response = mapper.toOptionResponse(option);
                    // Option không có cột riêng cho contentFormat, giá trị
                    // luôn được sao chép từ câu hỏi cha.
                    response.setContentFormat(contentFormat);
                    return response;
                })
                .toList();
    }

    private void validateOptions(List<CreateOptionRequest> options) {
        if (options == null || options.stream().filter(option -> option != null && hasText(option.getContent())).count() < 2) {
            throw new AppException("Câu hỏi phải có ít nhất hai đáp án", HttpStatus.BAD_REQUEST);
        }
        long correctCount = options.stream()
                .filter(option -> option != null && hasText(option.getContent()))
                .filter(CreateOptionRequest::isCorrected)
                .count();
        if (correctCount != 1) {
            throw new AppException("Câu hỏi phải có đúng một đáp án đúng", HttpStatus.BAD_REQUEST);
        }
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

    private void hideCorrectAnswers(QuestionResponse response) {
        response.setCorrectOptionId(null);
        if (response.getOptions() == null) {
            return;
        }
        response.getOptions().forEach(option -> option.setCorrected(false));
    }

    private List<QuestionSkillResponse> toSkillResponses(List<QuestionSkill> questionSkills) {
        if (questionSkills == null) {
            return Collections.emptyList();
        }

        return toSkillResponses(questionSkills, loadSkillNames(questionSkills));
    }

    private Map<String, String> loadSkillNames(List<QuestionSkill> questionSkills) {
        if (questionSkills == null || questionSkills.isEmpty()) return Map.of();
        return skillRepository.findByIds(questionSkills.stream().map(QuestionSkill::getSkillId).distinct().toList())
                .stream().collect(java.util.stream.Collectors.toMap(Skill::getSkillId, Skill::getName));
    }

    private List<QuestionSkillResponse> toSkillResponses(
            List<QuestionSkill> questionSkills, Map<String, String> skillNames) {
        if (questionSkills == null) return Collections.emptyList();
        return questionSkills.stream()
                .map(questionSkill -> {
                    return QuestionSkillResponse.builder()
                            .skillId(questionSkill.getSkillId())
                            .name(skillNames.get(questionSkill.getSkillId()))
                            .weight(questionSkill.getWeight())
                            .primary(questionSkill.isPrimary())
                            .build();
                })
                .toList();
    }

    private boolean isQuestionBankPlacement(String subjectId, String chapterId) {
        return (hasText(subjectId) && (ContentConstants.QUESTION_BANK_COURSE_CODE.equalsIgnoreCase(subjectId)
                || ContentConstants.QUESTION_BANK_NAME.equalsIgnoreCase(subjectId)))
                || (hasText(chapterId) && (ContentConstants.QUESTION_BANK_COURSE_CODE.equalsIgnoreCase(chapterId)
                || ContentConstants.QUESTION_BANK_CHAPTER_NAME.equalsIgnoreCase(chapterId)));
    }

    private void validateQuestionPlacement(String subjectId, String chapterId) {
        if (!hasText(subjectId) || !hasText(chapterId)) {
            throw new AppException(
                    "Vui lòng chọn môn học và chương cho câu hỏi",
                    HttpStatus.BAD_REQUEST);
        }

        contentRepository.findSubjectById(subjectId)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy môn học",
                        HttpStatus.NOT_FOUND));

        Chapter chapter = contentRepository.findChapterById(chapterId)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy chương học",
                        HttpStatus.NOT_FOUND));

        if (!subjectId.equals(chapter.getSubjectId())) {
            throw new AppException(
                    "Chương học không thuộc môn học đã chọn",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private void validateSkillMappings(List<QuestionSkillMappingRequest> skills, String chapterId) {
        if (skills == null || skills.isEmpty()) {
            return;
        }

        Set<String> seenSkillIds = new HashSet<>();
        Set<String> chapterSkillIds = chapterSkillRepository.findByChapterId(chapterId).stream()
                .map(chapterSkill -> chapterSkill.getSkillId()).collect(java.util.stream.Collectors.toSet());
        double weightSum = 0.0;

        long primaryCount = skills.stream()
                .filter(skill -> skill != null)
                .filter(QuestionSkillMappingRequest::isPrimary)
                .count();

        if (primaryCount != 1) {
            throw new AppException(
                    "Câu hỏi có kỹ năng phải có đúng một kỹ năng chính",
                    HttpStatus.BAD_REQUEST);
        }

        for (QuestionSkillMappingRequest skill : skills) {
            if (skill == null || !hasText(skill.getSkillId())) {
                throw new AppException(
                        "Kỹ năng của câu hỏi không hợp lệ",
                        HttpStatus.BAD_REQUEST);
            }

            if (!seenSkillIds.add(skill.getSkillId())) {
                throw new AppException(
                        "Một kỹ năng không được gắn lặp lại trong cùng câu hỏi",
                        HttpStatus.BAD_REQUEST);
            }

            if (!chapterSkillIds.contains(skill.getSkillId())) {
                throw new AppException(
                        "Kỹ năng của câu hỏi phải thuộc danh sách kỹ năng của chương",
                        HttpStatus.BAD_REQUEST);
            }

            skillRepository.findById(skill.getSkillId())
                    .orElseThrow(() -> new AppException(
                            "Không tìm thấy kỹ năng được gắn với câu hỏi",
                            HttpStatus.NOT_FOUND));

            Double weight = skill.getWeight();
            if (weight == null || !Double.isFinite(weight) || weight < 0) {
                throw new AppException(
                        "Trọng số kỹ năng phải là số không âm",
                        HttpStatus.BAD_REQUEST);
            }

            weightSum += weight;
        }

        if (Math.abs(weightSum - 1.0) > WEIGHT_SUM_TOLERANCE) {
            throw new AppException(
                    "Tổng trọng số kỹ năng của câu hỏi phải bằng 1",
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    @Transactional
    public void assignQuestionPlacement(String questionId, AssignQuestionPlacementRequest request) {
        validateQuestionPlacement(request.getSubjectId(), request.getChapterId());

        Question question = quizRepository.findQuestionById(questionId)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy câu hỏi",
                        HttpStatus.NOT_FOUND));

        question.setSubjectId(request.getSubjectId());
        question.setChapterId(request.getChapterId());
        quizRepository.save(question);

        // Clean up existing skills if they don't match the new chapter's skills
        List<QuestionSkill> currentSkills = quizRepository.findQuestionSkillsByQuestionId(questionId);
        if (!currentSkills.isEmpty()) {
            Set<String> chapterSkillIds = chapterSkillRepository.findByChapterId(request.getChapterId()).stream()
                    .map(vn.id.quiz.quizcitbackend.modules.content.presentation.model.ChapterSkill::getSkillId)
                    .collect(java.util.stream.Collectors.toSet());
            boolean allValid = currentSkills.stream().allMatch(s -> chapterSkillIds.contains(s.getSkillId()));
            if (!allValid) {
                quizRepository.deleteQuestionSkillsByQuestionId(questionId);
            }
        }
    }

    @Override
    @Transactional
    public void bulkAssignQuestionPlacement(BulkAssignQuestionPlacementRequest request) {
        if (request.getQuestionIds() == null || request.getQuestionIds().isEmpty()) {
            throw new AppException("Danh sách ID câu hỏi không được để trống", HttpStatus.BAD_REQUEST);
        }

        validateQuestionPlacement(request.getSubjectId(), request.getChapterId());

        List<Question> questions = quizRepository.findQuestionsByIds(request.getQuestionIds());
        if (questions.size() != request.getQuestionIds().stream().distinct().count()) {
            throw new AppException("Một hoặc nhiều câu hỏi không tồn tại", HttpStatus.NOT_FOUND);
        }

        Set<String> chapterSkillIds = chapterSkillRepository.findByChapterId(request.getChapterId()).stream()
                .map(vn.id.quiz.quizcitbackend.modules.content.presentation.model.ChapterSkill::getSkillId)
                .collect(java.util.stream.Collectors.toSet());

        for (Question question : questions) {
            question.setSubjectId(request.getSubjectId());
            question.setChapterId(request.getChapterId());
            quizRepository.save(question);

            List<QuestionSkill> currentSkills = quizRepository.findQuestionSkillsByQuestionId(question.getQuestionId());
            if (!currentSkills.isEmpty()) {
                boolean allValid = currentSkills.stream().allMatch(s -> chapterSkillIds.contains(s.getSkillId()));
                if (!allValid) {
                    quizRepository.deleteQuestionSkillsByQuestionId(question.getQuestionId());
                }
            }
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

}

package vn.id.quiz.quizcitbackend.modules.content.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.ChapterResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.CreateChapterRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.UpdateChapterRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.CreateSubjectRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.SubjectResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.UpdateSubjectRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.theory.CreateTheoryRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.theory.TheoryResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.theory.UpdateTheoryRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Theory;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.ChapterSkill;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.UserUnlockedChapter;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.ChapterSkillRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.ChapterSkillResponse;
import vn.id.quiz.quizcitbackend.modules.chatbot.service.TheoryIndexingService;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaChapterSkillRepository;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaUserUnlockedChapterRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.persistence.JpaUserSkillAbilityRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.SkillRepository;
import vn.id.quiz.quizcitbackend.shared.security.CustomUserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.*;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import vn.id.quiz.quizcitbackend.shared.dto.ReorderRequest;

@Service
@RequiredArgsConstructor
public class ContentServiceImplement implements ContentService {
    private final ContentRepository contentRepository;
    private final ContentMapper mapper;
    private final QuizRepository quizRepository;
    private final SubjectVisibilityGuard visibilityGuard;
    private final JpaChapterSkillRepository chapterSkillRepository;
    private final JpaUserUnlockedChapterRepository unlockedChapterRepository;
    private final JpaUserSkillAbilityRepository userSkillAbilityRepository;
    private final SkillRepository skillRepository;
    private final TheoryIndexingService theoryIndexingService;

    // =========================================================================
    // Phase 2 (P1): Lấy danh sách đề thi thuộc về 1 môn học
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public List<QuizSummaryResponse> getQuizzesBySubject(String subjectId) {
        visibilityGuard.requireAccessibleSubject(subjectId);
        List<Quiz> quizzes = quizRepository.findNonAdaptiveQuizzesBySubjectId(subjectId);
        return quizzes.stream()
                .map(QuizSummaryResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> getAllSubject(){
        List<Subject> subjects = visibilityGuard.isAdministrator()
                ? contentRepository.findAllSubject()
                : contentRepository.findAllVisibleSubject();
        return mapper.toSubjectResponseList(subjects.stream()
                .filter(s -> !ContentConstants.QUESTION_BANK_COURSE_CODE.equalsIgnoreCase(s.getCourseCode())
                        && !ContentConstants.QUESTION_BANK_NAME.equalsIgnoreCase(s.getName()))
                .toList());
    }
    @Override
    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(String subjectId){
        Subject subject = visibilityGuard.requireVisibleSubjectEntity(subjectId);
        if (ContentConstants.QUESTION_BANK_COURSE_CODE.equalsIgnoreCase(subject.getCourseCode())
                || ContentConstants.QUESTION_BANK_NAME.equalsIgnoreCase(subject.getName())) {
            throw new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        return mapper.toSubjectResponse(subject);
    }
    @Override
    @Transactional(readOnly = true)
    public ChapterResponse getChapterById(String chapterId){
        Chapter chapter = visibilityGuard.requireVisibleChapterEntity(chapterId);
        contentRepository.findSubjectById(chapter.getSubjectId()).ifPresent(parentSubject -> {
            if (ContentConstants.QUESTION_BANK_COURSE_CODE.equalsIgnoreCase(parentSubject.getCourseCode())
                    || ContentConstants.QUESTION_BANK_NAME.equalsIgnoreCase(parentSubject.getName())) {
                throw new AppException(ErrorMessages.CHAPTER_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
        });
        return toChapterResponse(chapter);
    }
    @Override
    @Transactional(readOnly = true)
    public TheoryResponse getTheoryById(String theoryId){
        Theory theory = visibilityGuard.requireVisibleTheoryEntity(theoryId);
        return mapper.toTheoryResponse(theory);
    }

    @Override
    @Transactional
    public SubjectResponse createSubject(CreateSubjectRequest request){
        if (ContentConstants.QUESTION_BANK_COURSE_CODE.equalsIgnoreCase(request.courseCode())
                || ContentConstants.QUESTION_BANK_NAME.equalsIgnoreCase(request.name())) {
            throw new AppException("Không thể tạo môn học trùng tên hoặc mã với Ngân hàng câu hỏi mặc định", HttpStatus.BAD_REQUEST);
        }
        if(contentRepository.existsByCourseCode(request.courseCode())){
            throw new AppException("Mã học phần đã tồn tại.", HttpStatus.CONFLICT);
        }
        int displayOrder = positiveOrDefault(request.displayOrder(), contentRepository.nextSubjectDisplayOrder());
        Subject subject = Subject.create(request.courseCode(), request.name(), request.description(), displayOrder);
        return mapper.toSubjectResponse(contentRepository.save(subject));
    }

    @Override
    @Transactional
    public ChapterResponse addChapter(String subjectId, CreateChapterRequest request){
        Subject subject = contentRepository.findSubjectById(subjectId)
                .orElseThrow(() -> new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND));
        if (ContentConstants.QUESTION_BANK_COURSE_CODE.equalsIgnoreCase(subject.getCourseCode())
                || ContentConstants.QUESTION_BANK_NAME.equalsIgnoreCase(subject.getName())) {
            throw new AppException("Không thể thêm chương vào Ngân hàng câu hỏi mặc định", HttpStatus.BAD_REQUEST);
        }
        int displayOrder = positiveOrDefault(request.displayOrder(), contentRepository.nextChapterDisplayOrder(subjectId));
        Chapter response = Chapter.create(subjectId, request.name(), displayOrder);
        contentRepository.save(response);
        replaceChapterSkills(response.getChapterId(), request.skills(), false);
        return toChapterResponse(response);
    }
    @Override
    @Transactional
    public TheoryResponse addTheory(String chapterId, CreateTheoryRequest request){
        Chapter chapter = contentRepository.findChapterById(chapterId)
                .orElseThrow(() -> new AppException(ErrorMessages.CHAPTER_NOT_FOUND, HttpStatus.NOT_FOUND));
        contentRepository.findSubjectById(chapter.getSubjectId()).ifPresent(parentSubject -> {
            if (ContentConstants.QUESTION_BANK_COURSE_CODE.equalsIgnoreCase(parentSubject.getCourseCode())
                    || ContentConstants.QUESTION_BANK_NAME.equalsIgnoreCase(parentSubject.getName())) {
                throw new AppException("Không thể thêm lý thuyết vào Ngân hàng câu hỏi mặc định", HttpStatus.BAD_REQUEST);
            }
        });

        Theory response = Theory.create(chapterId, request.content(), request.contentFormat());
        contentRepository.save(response);
        theoryIndexingService.index(
                response.getTheoryId(), chapterId, request.content(), request.contentFormat());
        return mapper.toTheoryResponse(response);
    }


    @Override
    @Transactional
    public List<ChapterResponse> getAllChapters(String subjectId){
        Subject subject = visibilityGuard.requireVisibleSubjectEntity(subjectId);
        if (ContentConstants.QUESTION_BANK_COURSE_CODE.equalsIgnoreCase(subject.getCourseCode())
                || ContentConstants.QUESTION_BANK_NAME.equalsIgnoreCase(subject.getName())) {
            throw new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        visibilityGuard.requireAccessibleSubject(subjectId);
        return toAccessibleChapterResponses(contentRepository.findAllChapter(subjectId));
    }

    @Override
    @Transactional
    public List<ChapterResponse> getAllAccessibleChapters() {
        boolean administrator = visibilityGuard.isAdministrator();
        List<Chapter> chapters = administrator
                ? contentRepository.findAllChapters(contentRepository.findAllSubject().stream()
                        .filter(s -> !ContentConstants.QUESTION_BANK_COURSE_CODE.equalsIgnoreCase(s.getCourseCode())
                                && !ContentConstants.QUESTION_BANK_NAME.equalsIgnoreCase(s.getName()))
                        .map(Subject::getSubjectId).toList())
                : contentRepository.findAccessibleChapters(currentUserId());
        return toAccessibleChapterResponses(chapters, administrator);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TheoryResponse> getAllTheories(String chapterId){
        visibilityGuard.requireVisibleChapter(chapterId);
        List<Theory> theories = contentRepository.findAllTheory(chapterId);
        return mapper.toTheoryResponseList(theories);
    }

    @Override
    @Transactional
    public List<TheoryResponse> getAllTheoriesBySubject(String subjectId) {
        List<String> chapterIds = getAllChapters(subjectId).stream().map(ChapterResponse::chapterId).toList();
        return mapper.toTheoryResponseList(contentRepository.findAllTheories(chapterIds));
    }

    @Override
    public int reindexTheoriesBySubject(String subjectId) {
        if (!contentRepository.existsBySubjectId(subjectId)) {
            throw new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        List<String> chapterIds = contentRepository.findAllChapter(subjectId).stream()
                .map(Chapter::getChapterId)
                .toList();
        int indexedChunks = 0;
        for (Theory theory : contentRepository.findAllTheories(chapterIds)) {
            indexedChunks += theoryIndexingService.reindex(
                    theory.getTheoryId(), theory.getChapterId(),
                    theory.getContent(), theory.getContentFormat());
        }
        return indexedChunks;
    }
    @Transactional
    @Override
    public void updateSubject(String subjectId, UpdateSubjectRequest request){
        Subject subject = contentRepository.findSubjectById(subjectId)
                .orElseThrow(()-> new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND));
        if (ContentConstants.QUESTION_BANK_COURSE_CODE.equalsIgnoreCase(subject.getCourseCode())
                || ContentConstants.QUESTION_BANK_NAME.equalsIgnoreCase(subject.getName())) {
            throw new AppException("Không thể chỉnh sửa Ngân hàng câu hỏi mặc định", HttpStatus.BAD_REQUEST);
        }
        if (ContentConstants.QUESTION_BANK_COURSE_CODE.equalsIgnoreCase(request.courseCode())
                || ContentConstants.QUESTION_BANK_NAME.equalsIgnoreCase(request.name())) {
            throw new AppException("Không thể đổi tên hoặc mã thành Ngân hàng câu hỏi mặc định", HttpStatus.BAD_REQUEST);
        }
        int displayOrder = positiveOrDefault(request.displayOrder(), subject.getDisplayOrder());
        subject.updateSubject(request.courseCode(), request.name(), request.description(), request.visible(), displayOrder);
        contentRepository.save(subject);
    }
    @Transactional
    @Override
    public void updateChapter(String chapterId, UpdateChapterRequest request){
        Chapter chapter = contentRepository.findChapterById(chapterId)
                        .orElseThrow(()->new AppException(ErrorMessages.CHAPTER_NOT_FOUND, HttpStatus.NOT_FOUND));
        contentRepository.findSubjectById(chapter.getSubjectId()).ifPresent(parentSubject -> {
            if (ContentConstants.QUESTION_BANK_COURSE_CODE.equalsIgnoreCase(parentSubject.getCourseCode())
                    || ContentConstants.QUESTION_BANK_NAME.equalsIgnoreCase(parentSubject.getName())) {
                throw new AppException("Không thể chỉnh sửa Ngân hàng câu hỏi mặc định", HttpStatus.BAD_REQUEST);
            }
        });

        int displayOrder = positiveOrDefault(request.displayOrder(), chapter.getDisplayOrder());
        chapter.updateChapter(request.name(), displayOrder);
        contentRepository.save(chapter);
        if (request.skills() != null) {
            replaceChapterSkills(chapterId, request.skills(), true);
        }
    }
    @Transactional
    @Override
    public void updateTheory(String theoryId, UpdateTheoryRequest request){
        Theory theory = contentRepository.findTheoryById(theoryId)
                .orElseThrow(()-> new AppException("Không tìm thấy nội dung lý thuyết được yêu cầu", HttpStatus.NOT_FOUND));
        contentRepository.findChapterById(theory.getChapterId()).ifPresent(chapter -> {
            contentRepository.findSubjectById(chapter.getSubjectId()).ifPresent(parentSubject -> {
                if (ContentConstants.QUESTION_BANK_COURSE_CODE.equalsIgnoreCase(parentSubject.getCourseCode())
                        || ContentConstants.QUESTION_BANK_NAME.equalsIgnoreCase(parentSubject.getName())) {
                    throw new AppException("Không thể chỉnh sửa lý thuyết của Ngân hàng câu hỏi mặc định", HttpStatus.BAD_REQUEST);
                }
            });
        });
        theory.updateTheory(request.content(), request.contentFormat());
        contentRepository.save(theory);
        theoryIndexingService.reindex(
                theoryId, theory.getChapterId(), request.content(), request.contentFormat());
    }

    @Override
    @Transactional
    public void deleteSubject(String subjectId){
        Subject subject = contentRepository.findSubjectById(subjectId)
                .orElseThrow(() -> new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND));

        if (ContentConstants.QUESTION_BANK_COURSE_CODE.equalsIgnoreCase(subject.getCourseCode())
                || ContentConstants.QUESTION_BANK_NAME.equalsIgnoreCase(subject.getName())) {
            throw new AppException("Không thể xóa Ngân hàng câu hỏi mặc định", HttpStatus.BAD_REQUEST);
        }

        Subject bankSubject = contentRepository.getOrCreateQuestionBankSubject();
        Chapter bankChapter = contentRepository.getOrCreateQuestionBankChapter(bankSubject.getSubjectId());

        // 1. Reassign all questions in the subject to Question Bank (subject_id = bankSubjectId, chapter_id = bankChapterId)
        quizRepository.reassignQuestionsBySubjectId(
                subjectId, bankSubject.getSubjectId(), bankChapter.getChapterId());

        // 2. Find and delete all chapters in the subject
        List<Chapter> chapters = contentRepository.findAllChapter(subjectId);
        if (!chapters.isEmpty()) {
            List<String> chapterIds = chapters.stream().map(Chapter::getChapterId).toList();
            quizRepository.reassignQuestionsByChapterIds(
                    chapterIds, bankSubject.getSubjectId(), bankChapter.getChapterId());

            for (String chapterId : chapterIds) {
                List<Theory> theories = contentRepository.findAllTheory(chapterId);
                for (Theory theory : theories) {
                    contentRepository.deleteTheoryById(theory.getTheoryId());
                    theoryIndexingService.delete(theory.getTheoryId());
                }
                chapterSkillRepository.deleteByChapterId(chapterId);
                unlockedChapterRepository.deleteByChapterId(chapterId);
                contentRepository.deleteChapterById(chapterId);
            }
        }

        // 3. Delete quizzes in this subject
        List<Quiz> quizzes = quizRepository.findQuizzesBySubjectId(subjectId);
        for (Quiz quiz : quizzes) {
            quizRepository.deleteQuizQuestionsByQuizId(quiz.getQuizId());
            quizRepository.deleteQuizById(quiz.getQuizId());
        }

        // 4. Delete the subject itself
        contentRepository.deleteSubjectById(subjectId);
    }

    @Override
    @Transactional
    public void deleteChapter(String chapterId) {
        Chapter chapter = contentRepository.findChapterById(chapterId)
                .orElseThrow(() -> new AppException(ErrorMessages.CHAPTER_NOT_FOUND, HttpStatus.NOT_FOUND));

        contentRepository.findSubjectById(chapter.getSubjectId()).ifPresent(parentSubject -> {
            if ((ContentConstants.QUESTION_BANK_COURSE_CODE.equalsIgnoreCase(parentSubject.getCourseCode())
                    || ContentConstants.QUESTION_BANK_NAME.equalsIgnoreCase(parentSubject.getName()))
                    && ContentConstants.QUESTION_BANK_CHAPTER_NAME.equalsIgnoreCase(chapter.getName())) {
                throw new AppException("Không thể xóa Ngân hàng câu hỏi mặc định", HttpStatus.BAD_REQUEST);
            }
        });

        Subject bankSubject = contentRepository.getOrCreateQuestionBankSubject();
        Chapter bankChapter = contentRepository.getOrCreateQuestionBankChapter(bankSubject.getSubjectId());

        // 1. Reassign all questions belonging to this chapter to Question Bank
        quizRepository.reassignQuestionsByChapterId(
                chapterId, bankSubject.getSubjectId(), bankChapter.getChapterId());

        // 2. Delete theories in chapter
        List<Theory> theories = contentRepository.findAllTheory(chapterId);
        for (Theory theory : theories) {
            contentRepository.deleteTheoryById(theory.getTheoryId());
            theoryIndexingService.delete(theory.getTheoryId());
        }

        // 3. Delete chapter skills and unlocked chapters
        chapterSkillRepository.deleteByChapterId(chapterId);
        unlockedChapterRepository.deleteByChapterId(chapterId);

        // 4. Delete quizzes in this chapter
        List<Quiz> quizzes = quizRepository.findQuizzesByChapterId(chapterId);
        for (Quiz quiz : quizzes) {
            quizRepository.deleteQuizQuestionsByQuizId(quiz.getQuizId());
            quizRepository.deleteQuizById(quiz.getQuizId());
        }

        // 5. Delete chapter
        contentRepository.deleteChapterById(chapterId);
    }

    @Override
    @Transactional
    public void deleteTheory(String theoryId) {
        contentRepository.deleteTheoryById(theoryId);
        theoryIndexingService.delete(theoryId);
    }

    @Override
    @Transactional
    public void reorderSubjects(ReorderRequest request) {
        Map<String, Subject> subjectsById = contentRepository.findSubjectsByIds(
                request.items().stream().map(item -> item.id()).toList()).stream()
                .collect(Collectors.toMap(Subject::getSubjectId, subject -> subject));
        Map<String, Integer> displayOrders = new java.util.LinkedHashMap<>();
        request.items().forEach(item -> {
            Subject subject = java.util.Optional.ofNullable(subjectsById.get(item.id()))
                    .orElseThrow(() -> new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND));
            displayOrders.put(subject.getSubjectId(), item.displayOrder());
        });
        contentRepository.updateSubjectDisplayOrders(displayOrders);
    }

    @Override
    @Transactional
    public void reorderChapters(String subjectId, ReorderRequest request) {
        if (!contentRepository.existsBySubjectId(subjectId)) {
            throw new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        Map<String, Chapter> chaptersById = contentRepository.findChaptersByIds(
                request.items().stream().map(item -> item.id()).toList()).stream()
                .collect(Collectors.toMap(Chapter::getChapterId, chapter -> chapter));
        Map<String, Integer> displayOrders = new java.util.LinkedHashMap<>();
        request.items().forEach(item -> {
            Chapter chapter = java.util.Optional.ofNullable(chaptersById.get(item.id()))
                    .filter(value -> subjectId.equals(value.getSubjectId()))
                    .orElseThrow(() -> new AppException(ErrorMessages.CHAPTER_NOT_FOUND, HttpStatus.NOT_FOUND));
            displayOrders.put(chapter.getChapterId(), item.displayOrder());
        });
        contentRepository.updateChapterDisplayOrders(displayOrders);
    }

    private int positiveOrDefault(Integer requested, int fallback) {
        return requested != null && requested > 0 ? requested : fallback;
    }

    private boolean canUnlock(String userId, String chapterId) {
        if (userId != null && unlockedChapterRepository.existsByUserIdAndChapterId(userId, chapterId)) return true;
        List<ChapterSkill> requirements = chapterSkillRepository.findByChapterId(chapterId);
        Map<String, Double> thetaBySkill = userId == null ? Map.of() : userSkillAbilityRepository.findByUserId(userId)
                .stream().collect(Collectors.toMap(a -> a.getSkillId(), a -> a.getTheta() == null ? 0.0 : a.getTheta()));
        boolean eligible = requirements.stream()
                .allMatch(requirement -> thetaBySkill.getOrDefault(requirement.getSkillId(), 0.0) >= requirement.getRequiredTheta());
        if (eligible && userId != null) {
            unlockedChapterRepository.insertIfAbsent(userId, chapterId);
        }
        return eligible;
    }

    private List<ChapterResponse> toAccessibleChapterResponses(List<Chapter> chapters) {
        return toAccessibleChapterResponses(chapters, visibilityGuard.isAdministrator());
    }

    private List<ChapterResponse> toAccessibleChapterResponses(List<Chapter> chapters, boolean administrator) {
        if (chapters.isEmpty()) return List.of();
        List<String> chapterIds = chapters.stream().map(Chapter::getChapterId).toList();
        Map<String, List<ChapterSkill>> requirementsByChapter = chapterSkillRepository
                .findByChapterIdIn(chapterIds).stream().collect(Collectors.groupingBy(ChapterSkill::getChapterId));
        Set<String> skillIds = requirementsByChapter.values().stream().flatMap(List::stream)
                .map(ChapterSkill::getSkillId).collect(Collectors.toSet());
        Map<String, String> skillNames = skillRepository.findByIds(skillIds).stream()
                .collect(Collectors.toMap(skill -> skill.getSkillId(), skill -> skill.getName()));

        List<Chapter> accessible = chapters;
        if (!administrator) {
            String userId = currentUserId();
            Set<String> unlocked = userId == null ? Set.of() : unlockedChapterRepository
                    .findByUserIdAndChapterIdIn(userId, chapterIds).stream()
                    .map(UserUnlockedChapter::getChapterId).collect(Collectors.toSet());
            Map<String, Double> thetaBySkill = userId == null ? Map.of() : userSkillAbilityRepository
                    .findByUserId(userId).stream().collect(Collectors.toMap(
                            ability -> ability.getSkillId(),
                            ability -> ability.getTheta() == null ? 0.0 : ability.getTheta()));
            accessible = chapters.stream().filter(chapter -> unlocked.contains(chapter.getChapterId())
                    || requirementsByChapter.getOrDefault(chapter.getChapterId(), List.of()).stream()
                    .allMatch(requirement -> thetaBySkill.getOrDefault(requirement.getSkillId(), 0.0)
                            >= requirement.getRequiredTheta())).toList();
            if (userId != null) {
                List<String> newlyUnlocked = accessible.stream().map(Chapter::getChapterId)
                        .filter(id -> !unlocked.contains(id)).toList();
                if (!newlyUnlocked.isEmpty()) {
                    unlockedChapterRepository.insertAllIfAbsent(userId, newlyUnlocked);
                }
            }
        }
        return accessible.stream().map(chapter -> toChapterResponse(
                chapter, requirementsByChapter.getOrDefault(chapter.getChapterId(), List.of()), skillNames)).toList();
    }

    private String currentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof CustomUserPrincipal principal
                ? principal.getUserId() : null;
    }

    private void replaceChapterSkills(String chapterId, List<ChapterSkillRequest> requests, boolean guardRemoval) {
        List<ChapterSkillRequest> safeRequests = requests == null ? List.of() : requests;
        Set<String> ids = new HashSet<>();
        for (ChapterSkillRequest request : safeRequests) {
            if (request == null || !ids.add(request.skillId())) {
                throw new AppException("Kỹ năng của chương không hợp lệ hoặc bị trùng", HttpStatus.BAD_REQUEST);
            }
            if (!Double.isFinite(request.requiredTheta())) {
                throw new AppException("Theta yêu cầu phải là một số hợp lệ", HttpStatus.BAD_REQUEST);
            }
        }
        Set<String> existingSkillIds = skillRepository.findByIds(ids).stream()
                .map(skill -> skill.getSkillId()).collect(Collectors.toSet());
        if (existingSkillIds.size() != ids.size()) {
            throw new AppException("Không tìm thấy kỹ năng của chương", HttpStatus.NOT_FOUND);
        }
        if (guardRemoval) {
            Set<String> removed = chapterSkillRepository.findByChapterId(chapterId).stream()
                    .map(ChapterSkill::getSkillId).filter(id -> !ids.contains(id)).collect(Collectors.toSet());
            if (!removed.isEmpty() && chapterSkillRepository.existsQuestionUsingRemovedSkills(chapterId, removed)) {
                throw new AppException("Không thể xóa kỹ năng đang được câu hỏi trong chương sử dụng. Hãy xóa câu hỏi trước.", HttpStatus.CONFLICT);
            }
        }
        chapterSkillRepository.deleteByChapterId(chapterId);
        chapterSkillRepository.saveAll(safeRequests.stream()
                .map(request -> ChapterSkill.create(chapterId, request.skillId(), request.requiredTheta())).toList());
    }

    private ChapterResponse toChapterResponse(Chapter chapter) {
        List<ChapterSkill> requirements = chapterSkillRepository.findByChapterId(chapter.getChapterId());
        Map<String, String> names = skillRepository.findByIds(requirements.stream().map(ChapterSkill::getSkillId).toList())
                .stream().collect(Collectors.toMap(skill -> skill.getSkillId(), skill -> skill.getName()));
        return toChapterResponse(chapter, requirements, names);
    }

    private ChapterResponse toChapterResponse(
            Chapter chapter, List<ChapterSkill> requirements, Map<String, String> skillNames) {
        List<ChapterSkillResponse> skills = requirements.stream()
                .map(item -> new ChapterSkillResponse(item.getSkillId(), skillNames.get(item.getSkillId()),
                        item.getRequiredTheta())).toList();
        return new ChapterResponse(chapter.getChapterId(), chapter.getSubjectId(), chapter.getName(),
                chapter.getCreatedAt(), chapter.getDisplayOrder(), skills);
    }
}

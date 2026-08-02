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
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.ChapterSkillRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.ChapterSkillResponse;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaChapterSkillRepository;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaUserUnlockedChapterRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.persistence.JpaUserSkillAbilityRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.SkillRepository;
import vn.id.quiz.quizcitbackend.shared.security.CustomUserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.*;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizType;
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

    // =========================================================================
    // Phase 2 (P1): Lấy danh sách đề thi thuộc về 1 môn học
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public List<QuizSummaryResponse> getQuizzesBySubject(String subjectId) {
        visibilityGuard.requireVisibleSubject(subjectId);
        if (!contentRepository.existsBySubjectId(subjectId)) {
            throw new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        List<Quiz> quizzes = quizRepository.findQuizzesBySubjectId(subjectId);
        return quizzes.stream()
                .filter(quiz -> quiz.getType() != QuizType.ADAPTIVE)
                .map(QuizSummaryResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> getAllSubject(){
        List<Subject> subjects = visibilityGuard.isAdministrator()
                ? contentRepository.findAllSubject()
                : contentRepository.findAllVisibleSubject();
        return mapper.toSubjectResponseList(subjects);
    }
    @Override
    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(String subjectId){
        visibilityGuard.requireVisibleSubject(subjectId);
        Subject subject = contentRepository.findSubjectById(subjectId)
                .orElseThrow(()-> new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND));
        return mapper.toSubjectResponse(subject);
    }
    @Override
    @Transactional(readOnly = true)
    public ChapterResponse getChapterById(String chapterId){
        visibilityGuard.requireVisibleChapter(chapterId);
        Chapter chapter = contentRepository.findChapterById(chapterId)
                .orElseThrow(()-> new AppException(ErrorMessages.CHAPTER_NOT_FOUND, HttpStatus.NOT_FOUND));
        return toChapterResponse(chapter);
    }
    @Override
    @Transactional(readOnly = true)
    public TheoryResponse getTheoryById(String theoryId){
        visibilityGuard.requireVisibleTheory(theoryId);
        Theory theory = contentRepository.findTheoryById(theoryId)
                .orElseThrow(()-> new AppException("Không tìm thấy nội dung lý thuyết được yêu cầu", HttpStatus.NOT_FOUND));
        return mapper.toTheoryResponse(theory);
    }

    @Override
    @Transactional
    public SubjectResponse createSubject(CreateSubjectRequest request){

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
        if(!contentRepository.existsBySubjectId(subjectId)){
            throw new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND);
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
        if(!contentRepository.existsByChapterId(chapterId)){
            throw new AppException(ErrorMessages.CHAPTER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        Theory response = Theory.create(chapterId, request.content(), request.contentFormat());
        contentRepository.save(response);
        return mapper.toTheoryResponse(response);
    }


    @Override
    @Transactional
    public List<ChapterResponse> getAllChapters(String subjectId){
        visibilityGuard.requireVisibleSubject(subjectId);
        if(!contentRepository.existsBySubjectId(subjectId)){
            throw new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        List<Chapter> chapters = contentRepository.findAllChapter(subjectId);
        if (!visibilityGuard.isAdministrator()) {
            String userId = currentUserId();
            chapters = chapters.stream().filter(chapter -> canUnlock(userId, chapter.getChapterId())).toList();
        }
        return chapters.stream().map(this::toChapterResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TheoryResponse> getAllTheories(String chapterId){
        visibilityGuard.requireVisibleChapter(chapterId);
        if(!contentRepository.existsByChapterId(chapterId)){
            throw new AppException(ErrorMessages.CHAPTER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        List<Theory> theories = contentRepository.findAllTheory(chapterId);
        return mapper.toTheoryResponseList(theories);
    }
    @Transactional
    @Override
    public void updateSubject(String subjectId, UpdateSubjectRequest request){
        Subject subject = contentRepository.findSubjectById(subjectId)
                .orElseThrow(()-> new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND));
        int displayOrder = positiveOrDefault(request.displayOrder(), subject.getDisplayOrder());
        subject.updateSubject(request.courseCode(), request.name(), request.description(), request.visible(), displayOrder);
        contentRepository.save(subject);
    }
    @Transactional
    @Override
    public void updateChapter(String chapterId, UpdateChapterRequest request){
        Chapter chapter = contentRepository.findChapterById(chapterId)
                        .orElseThrow(()->new AppException(ErrorMessages.CHAPTER_NOT_FOUND, HttpStatus.NOT_FOUND));

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
        theory.updateTheory(request.content(), request.contentFormat());
        contentRepository.save(theory);
    }

    @Override
    @Transactional
    public void deleteSubject(String subjectId){
        contentRepository.deleteSubjectById(subjectId);
    }
    @Override
    @Transactional
    public void deleteChapter(String chapterId) {
        contentRepository.deleteChapterById(chapterId);
    }

    @Override
    @Transactional
    public void deleteTheory(String theoryId) {
        contentRepository.deleteTheoryById(theoryId);
    }

    @Override
    @Transactional
    public void reorderSubjects(ReorderRequest request) {
        request.items().forEach(item -> {
            Subject subject = contentRepository.findSubjectById(item.id())
                    .orElseThrow(() -> new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND));
            subject.updateDisplayOrder(item.displayOrder());
            contentRepository.save(subject);
        });
    }

    @Override
    @Transactional
    public void reorderChapters(String subjectId, ReorderRequest request) {
        if (!contentRepository.existsBySubjectId(subjectId)) {
            throw new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        request.items().forEach(item -> {
            Chapter chapter = contentRepository.findChapterById(item.id())
                    .filter(value -> subjectId.equals(value.getSubjectId()))
                    .orElseThrow(() -> new AppException(ErrorMessages.CHAPTER_NOT_FOUND, HttpStatus.NOT_FOUND));
            chapter.updateDisplayOrder(item.displayOrder());
            contentRepository.save(chapter);
        });
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
            skillRepository.findById(request.skillId()).orElseThrow(() ->
                    new AppException("Không tìm thấy kỹ năng của chương", HttpStatus.NOT_FOUND));
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
        List<ChapterSkillResponse> skills = chapterSkillRepository.findByChapterId(chapter.getChapterId()).stream()
                .map(item -> new ChapterSkillResponse(item.getSkillId(),
                        skillRepository.findById(item.getSkillId()).map(skill -> skill.getName()).orElse(null),
                        item.getRequiredTheta()))
                .toList();
        return new ChapterResponse(chapter.getChapterId(), chapter.getSubjectId(), chapter.getName(),
                chapter.getCreatedAt(), chapter.getDisplayOrder(), skills);
    }
}

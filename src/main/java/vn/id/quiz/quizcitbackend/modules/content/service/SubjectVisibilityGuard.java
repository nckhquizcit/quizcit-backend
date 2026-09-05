package vn.id.quiz.quizcitbackend.modules.content.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.ChapterSkill;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Theory;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.UserUnlockedChapter;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaChapterSkillRepository;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaUserUnlockedChapterRepository;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaUserCourseEnrollmentRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.persistence.JpaUserSkillAbilityRepository;
import vn.id.quiz.quizcitbackend.shared.security.CustomUserPrincipal;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

@Component
@RequiredArgsConstructor(onConstructor_ = @org.springframework.beans.factory.annotation.Autowired)
public class SubjectVisibilityGuard {
    public static final String VIEW_AS_HEADER = "X-QuizCIT-View-As";
    public static final String USER_VIEW = "USER";

    private final ContentRepository contentRepository;
    private final QuizRepository quizRepository;
    private final JpaChapterSkillRepository chapterSkillRepository;
    private final JpaUserUnlockedChapterRepository unlockedChapterRepository;
    private final JpaUserSkillAbilityRepository userSkillAbilityRepository;
    private final JpaUserCourseEnrollmentRepository courseEnrollmentRepository;

    /** Compatibility constructor for isolated service unit tests. */
    public SubjectVisibilityGuard(ContentRepository contentRepository, QuizRepository quizRepository) {
        this.contentRepository = contentRepository;
        this.quizRepository = quizRepository;
        this.chapterSkillRepository = null;
        this.unlockedChapterRepository = null;
        this.userSkillAbilityRepository = null;
        this.courseEnrollmentRepository = null;
    }

    /** Compatibility constructor for chapter-unlock unit tests. */
    public SubjectVisibilityGuard(
            ContentRepository contentRepository,
            QuizRepository quizRepository,
            JpaChapterSkillRepository chapterSkillRepository,
            JpaUserUnlockedChapterRepository unlockedChapterRepository,
            JpaUserSkillAbilityRepository userSkillAbilityRepository) {
        this.contentRepository = contentRepository;
        this.quizRepository = quizRepository;
        this.chapterSkillRepository = chapterSkillRepository;
        this.unlockedChapterRepository = unlockedChapterRepository;
        this.userSkillAbilityRepository = userSkillAbilityRepository;
        this.courseEnrollmentRepository = null;
    }

    public boolean isAdministrator() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdministrator = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMINISTRATOR".equals(authority.getAuthority()));
        return isAdministrator && !isUserViewRequested();
    }

    public boolean isCourseEnrollmentCheckEnabled() {
        return courseEnrollmentRepository != null;
    }

    private boolean isUserViewRequested() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return false;
        }
        return USER_VIEW.equalsIgnoreCase(attributes.getRequest().getHeader(VIEW_AS_HEADER));
    }

    public void requireVisibleSubject(String subjectId) {
        requireVisibleSubjectEntity(subjectId);
    }

    public vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject requireVisibleSubjectEntity(String subjectId) {
        var subject = contentRepository.findSubjectById(subjectId)
                .orElseThrow(() -> new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND));
        if (!isAdministrator() && !subject.isVisible()) {
            // Return 404 so a hidden subject cannot be enumerated by ordinary users.
            throw new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        return subject;
    }

    /**
     * Kiểm tra quyền đọc/học nội dung của môn. Danh mục môn vẫn có thể được xem
     * trước khi đăng ký, nhưng chương, lý thuyết và bài kiểm tra thì không.
     */
    public void requireAccessibleSubject(String subjectId) {
        requireVisibleSubject(subjectId);
        if (isAdministrator()) {
            return;
        }

        // Chỉ null trong compatibility constructor của các unit test/service cũ.
        if (courseEnrollmentRepository == null) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication != null && authentication.getPrincipal() instanceof CustomUserPrincipal principal
                ? principal.getUserId() : null;
        if (userId == null || userId.isBlank()) {
            throw new AppException(ErrorMessages.LOGIN_REQUIRED, HttpStatus.UNAUTHORIZED);
        }
        if (!courseEnrollmentRepository.existsByUserIdAndSubjectId(userId, subjectId)) {
            throw new AppException(ErrorMessages.COURSE_ENROLLMENT_REQUIRED, HttpStatus.FORBIDDEN);
        }
    }

    public void requireVisibleChapter(String chapterId) {
        requireVisibleChapterEntity(chapterId);
    }

    public Chapter requireVisibleChapterEntity(String chapterId) {
        Chapter chapter = contentRepository.findChapterById(chapterId)
                .orElseThrow(() -> new AppException(ErrorMessages.CHAPTER_NOT_FOUND, HttpStatus.NOT_FOUND));
        requireAccessibleSubject(chapter.getSubjectId());
        return chapter;
    }

    public void requireVisibleTheory(String theoryId) {
        requireVisibleTheoryEntity(theoryId);
    }

    public Theory requireVisibleTheoryEntity(String theoryId) {
        Theory theory = contentRepository.findTheoryById(theoryId)
                .orElseThrow(() -> new AppException("Không tìm thấy nội dung lý thuyết được yêu cầu", HttpStatus.NOT_FOUND));
        requireVisibleChapter(theory.getChapterId());
        return theory;
    }

    public void requireVisibleQuiz(String quizId) {
        Quiz quiz = quizRepository.findQuizById(quizId)
                .orElseThrow(() -> new AppException(ErrorMessages.QUIZ_NOT_FOUND, HttpStatus.NOT_FOUND));
        requireAccessibleSubject(quiz.getSubjectId());
    }

    public boolean canAccessChapter(String chapterId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication != null && authentication.getPrincipal() instanceof CustomUserPrincipal principal
                ? principal.getUserId() : null;
        return canAccessChapter(userId, chapterId);
    }

    public boolean canAccessChapter(String userId, String chapterId) {
        if (chapterId == null || chapterId.isBlank()) return false;
        Chapter currentChapter = contentRepository.findChapterById(chapterId).orElse(null);
        if (currentChapter == null) return false;
        return getUnlockedChapterIds(userId, currentChapter.getSubjectId()).contains(chapterId);
    }

    public Set<String> getUnlockedChapterIds(String userId, String subjectId) {
        if (subjectId == null || subjectId.isBlank()) return Set.of();
        List<Chapter> chapters = contentRepository.findAllChapter(subjectId);
        if (chapters == null || chapters.isEmpty()) return Set.of();

        if (chapterSkillRepository == null) {
            return chapters.stream().map(Chapter::getChapterId).collect(Collectors.toSet());
        }

        List<String> chapterIds = chapters.stream().map(Chapter::getChapterId).toList();
        if (userId == null || userId.isBlank()) {
            return Set.of(chapters.get(0).getChapterId());
        }

        Map<String, Double> thetaBySkill = userSkillAbilityRepository == null
                ? Map.of()
                : userSkillAbilityRepository.findByUserId(userId).stream()
                        .collect(Collectors.toMap(
                                a -> a.getSkillId(),
                                a -> a.getTheta() == null ? 0.0 : a.getTheta()));

        Map<String, List<ChapterSkill>> skillsByChapter = chapterSkillRepository.findByChapterIdIn(chapterIds)
                .stream().collect(Collectors.groupingBy(ChapterSkill::getChapterId));

        Set<String> savedUnlocked = unlockedChapterRepository == null ? Set.of() :
                unlockedChapterRepository.findByUserIdAndChapterIdIn(userId, chapterIds).stream()
                        .map(UserUnlockedChapter::getChapterId).collect(Collectors.toSet());

        Set<String> unlocked = new LinkedHashSet<>();
        boolean chainActive = true;

        for (int i = 0; i < chapters.size(); i++) {
            Chapter ch = chapters.get(i);
            String chId = ch.getChapterId();

            if (i == 0) {
                unlocked.add(chId);
                List<ChapterSkill> reqs = skillsByChapter.getOrDefault(chId, List.of());
                boolean passed = reqs.isEmpty() || reqs.stream().allMatch(
                        r -> thetaBySkill.containsKey(r.getSkillId()) && thetaBySkill.get(r.getSkillId()) >= r.getRequiredTheta());
                if (!passed) {
                    chainActive = false;
                }
            } else {
                if (chainActive) {
                    unlocked.add(chId);
                    List<ChapterSkill> reqs = skillsByChapter.getOrDefault(chId, List.of());
                    boolean passed = reqs.isEmpty() || reqs.stream().allMatch(
                            r -> thetaBySkill.containsKey(r.getSkillId()) && thetaBySkill.get(r.getSkillId()) >= r.getRequiredTheta());
                    if (!passed) {
                        chainActive = false;
                    }
                } else {
                    break;
                }
            }
        }

        if (unlockedChapterRepository != null) {
            Set<String> invalidSaved = savedUnlocked.stream()
                    .filter(id -> !unlocked.contains(id))
                    .collect(Collectors.toSet());
            if (!invalidSaved.isEmpty()) {
                unlockedChapterRepository.deleteByUserIdAndChapterIdIn(userId, invalidSaved);
            }
            List<String> newlyUnlocked = unlocked.stream()
                    .filter(id -> !savedUnlocked.contains(id))
                    .toList();
            if (!newlyUnlocked.isEmpty()) {
                unlockedChapterRepository.insertAllIfAbsent(userId, newlyUnlocked);
            }
        }

        return unlocked;
    }
}

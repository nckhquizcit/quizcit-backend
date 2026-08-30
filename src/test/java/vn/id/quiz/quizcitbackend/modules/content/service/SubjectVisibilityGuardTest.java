package vn.id.quiz.quizcitbackend.modules.content.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.ChapterSkill;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaChapterSkillRepository;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaUserUnlockedChapterRepository;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaUserCourseEnrollmentRepository;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.UserSkillAbility;
import vn.id.quiz.quizcitbackend.modules.progress.repository.persistence.JpaUserSkillAbilityRepository;
import vn.id.quiz.quizcitbackend.shared.security.CustomUserPrincipal;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SubjectVisibilityGuardTest {

    private final ContentRepository contentRepository = mock(ContentRepository.class);
    private final QuizRepository quizRepository = mock(QuizRepository.class);
    private final SubjectVisibilityGuard guard = new SubjectVisibilityGuard(
            contentRepository,
            quizRepository);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void administratorKeepsAdministratorVisibilityWithoutPreviewHeader() {
        authenticateAs("ROLE_ADMINISTRATOR");

        assertThat(guard.isAdministrator()).isTrue();
    }

    @Test
    void administratorGetsUserVisibilityWhenPreviewHeaderIsPresent() {
        authenticateAs("ROLE_ADMINISTRATOR");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(SubjectVisibilityGuard.VIEW_AS_HEADER, SubjectVisibilityGuard.USER_VIEW);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertThat(guard.isAdministrator()).isFalse();
    }

    @Test
    void previewHeaderNeverPromotesAnOrdinaryUser() {
        authenticateAs("ROLE_USER");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(SubjectVisibilityGuard.VIEW_AS_HEADER, SubjectVisibilityGuard.USER_VIEW);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertThat(guard.isAdministrator()).isFalse();
    }

    @Test
    void ordinaryUserCannotAccessQuizFromHiddenSubject() {
        authenticateAs("ROLE_USER");
        stubQuizWithSubjectVisibility(false);

        assertThatThrownBy(() -> guard.requireVisibleQuiz("quiz-1"))
                .isInstanceOf(AppException.class)
                .hasMessage(ErrorMessages.SUBJECT_NOT_FOUND);
    }

    @Test
    void ordinaryUserMustEnrollBeforeAccessingCourseContent() {
        JpaUserCourseEnrollmentRepository enrollments = mock(JpaUserCourseEnrollmentRepository.class);
        SubjectVisibilityGuard enrollmentGuard = new SubjectVisibilityGuard(
                contentRepository, quizRepository, null, null, null, enrollments);
        authenticateAsUserPrincipal("user-1");
        when(contentRepository.findSubjectById("subject-1")).thenReturn(Optional.of(
                Subject.builder().subjectId("subject-1").courseCode("CT101")
                        .name("Môn học").visible(true).build()));
        when(enrollments.existsByUserIdAndSubjectId("user-1", "subject-1")).thenReturn(false);

        assertThatThrownBy(() -> enrollmentGuard.requireAccessibleSubject("subject-1"))
                .isInstanceOf(AppException.class)
                .hasMessage(ErrorMessages.COURSE_ENROLLMENT_REQUIRED)
                .extracting("status").isEqualTo(org.springframework.http.HttpStatus.FORBIDDEN.value());
    }

    @Test
    void enrolledUserCanAccessCourseContent() {
        JpaUserCourseEnrollmentRepository enrollments = mock(JpaUserCourseEnrollmentRepository.class);
        SubjectVisibilityGuard enrollmentGuard = new SubjectVisibilityGuard(
                contentRepository, quizRepository, null, null, null, enrollments);
        authenticateAsUserPrincipal("user-1");
        when(contentRepository.findSubjectById("subject-1")).thenReturn(Optional.of(
                Subject.builder().subjectId("subject-1").courseCode("CT101")
                        .name("Môn học").visible(true).build()));
        when(enrollments.existsByUserIdAndSubjectId("user-1", "subject-1")).thenReturn(true);

        enrollmentGuard.requireAccessibleSubject("subject-1");
    }

    @Test
    void administratorCanAccessQuizFromHiddenSubjectOutsidePreviewMode() {
        authenticateAs("ROLE_ADMINISTRATOR");
        stubQuizWithSubjectVisibility(false);

        guard.requireVisibleQuiz("quiz-1");
    }

    @Test
    void firstChapterIsAlwaysAccessible() {
        JpaChapterSkillRepository requirements = mock(JpaChapterSkillRepository.class);
        JpaUserUnlockedChapterRepository unlocked = mock(JpaUserUnlockedChapterRepository.class);
        JpaUserSkillAbilityRepository abilities = mock(JpaUserSkillAbilityRepository.class);
        SubjectVisibilityGuard accessGuard = new SubjectVisibilityGuard(
                contentRepository, quizRepository, requirements, unlocked, abilities);
        authenticateAsUserPrincipal("user-1");
        stubVisibleChapter();

        accessGuard.requireVisibleChapter("chapter-1");
    }

    @Test
    void eligibleUserCanAccessSubsequentChapterWhenPrecedingSkillsMet() {
        JpaChapterSkillRepository requirements = mock(JpaChapterSkillRepository.class);
        JpaUserUnlockedChapterRepository unlocked = mock(JpaUserUnlockedChapterRepository.class);
        JpaUserSkillAbilityRepository abilities = mock(JpaUserSkillAbilityRepository.class);
        SubjectVisibilityGuard accessGuard = new SubjectVisibilityGuard(
                contentRepository, quizRepository, requirements, unlocked, abilities);
        authenticateAsUserPrincipal("user-1");
        stubVisibleChapter();
        when(requirements.findByChapterIdIn(List.of("chapter-1", "chapter-2")))
                .thenReturn(List.of(ChapterSkill.create("chapter-1", "skill-1", 0.5)));
        UserSkillAbility ability = UserSkillAbility.create("user-1", "skill-1");
        ability.updateAbility(0.5);
        when(abilities.findByUserId("user-1")).thenReturn(List.of(ability));

        accessGuard.requireVisibleChapter("chapter-2");
    }

    @Test
    void ineligibleUserCannotAccessSubsequentChapterWhenPrecedingSkillsNotMet() {
        JpaChapterSkillRepository requirements = mock(JpaChapterSkillRepository.class);
        JpaUserUnlockedChapterRepository unlocked = mock(JpaUserUnlockedChapterRepository.class);
        JpaUserSkillAbilityRepository abilities = mock(JpaUserSkillAbilityRepository.class);
        SubjectVisibilityGuard accessGuard = new SubjectVisibilityGuard(
                contentRepository, quizRepository, requirements, unlocked, abilities);
        authenticateAsUserPrincipal("user-1");
        stubVisibleChapter();
        when(requirements.findByChapterIdIn(List.of("chapter-1", "chapter-2")))
                .thenReturn(List.of(ChapterSkill.create("chapter-1", "skill-1", 0.5)));
        UserSkillAbility ability = UserSkillAbility.create("user-1", "skill-1");
        ability.updateAbility(0.2);
        when(abilities.findByUserId("user-1")).thenReturn(List.of(ability));

        assertThatThrownBy(() -> accessGuard.requireVisibleChapter("chapter-2"))
                .isInstanceOf(AppException.class)
                .hasMessage(ErrorMessages.CHAPTER_NOT_FOUND);
    }

    @Test
    void previouslyUnlockedUserKeepsAccessAfterThetaDrops() {
        JpaChapterSkillRepository requirements = mock(JpaChapterSkillRepository.class);
        JpaUserUnlockedChapterRepository unlocked = mock(JpaUserUnlockedChapterRepository.class);
        JpaUserSkillAbilityRepository abilities = mock(JpaUserSkillAbilityRepository.class);
        SubjectVisibilityGuard accessGuard = new SubjectVisibilityGuard(
                contentRepository, quizRepository, requirements, unlocked, abilities);
        authenticateAsUserPrincipal("user-1");
        stubVisibleChapter();
        when(unlocked.existsByUserIdAndChapterId("user-1", "chapter-2")).thenReturn(true);

        accessGuard.requireVisibleChapter("chapter-2");
    }

    private void stubVisibleChapter() {
        Chapter ch1 = Chapter.builder().chapterId("chapter-1").subjectId("subject-1").name("Chương 1").displayOrder(1).build();
        Chapter ch2 = Chapter.builder().chapterId("chapter-2").subjectId("subject-1").name("Chương 2").displayOrder(2).build();
        when(contentRepository.findChapterById("chapter-1")).thenReturn(Optional.of(ch1));
        when(contentRepository.findChapterById("chapter-2")).thenReturn(Optional.of(ch2));
        when(contentRepository.findAllChapter("subject-1")).thenReturn(List.of(ch1, ch2));
        when(contentRepository.findSubjectById("subject-1")).thenReturn(Optional.of(
                Subject.builder().subjectId("subject-1").courseCode("CT101").name("Môn học").visible(true).build()));
    }

    private void authenticateAsUserPrincipal(String userId) {
        CustomUserPrincipal principal = new CustomUserPrincipal(userId, "student", "student@example.com", "USER", "ACTIVE");
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    private void stubQuizWithSubjectVisibility(boolean visible) {
        Quiz quiz = Quiz.builder()
                .quizId("quiz-1")
                .subjectId("subject-1")
                .build();
        Subject subject = Subject.builder()
                .subjectId("subject-1")
                .courseCode("CT101")
                .name("Lập trình căn bản")
                .visible(visible)
                .build();

        when(quizRepository.findQuizById("quiz-1")).thenReturn(Optional.of(quiz));
        when(contentRepository.findSubjectById("subject-1")).thenReturn(Optional.of(subject));
    }

    private void authenticateAs(String authority) {
        var authentication = new TestingAuthenticationToken(
                "test-user",
                null,
                List.of(new SimpleGrantedAuthority(authority)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}

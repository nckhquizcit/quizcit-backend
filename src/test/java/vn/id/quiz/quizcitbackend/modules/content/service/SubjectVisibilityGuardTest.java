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
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.UserUnlockedChapter;
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
        ability.updateAbility(0.5, 1.0);
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
        ability.updateAbility(0.2, 1.0);
        when(abilities.findByUserId("user-1")).thenReturn(List.of(ability));

        assertThat(accessGuard.canAccessChapter("user-1", "chapter-2")).isFalse();
        assertThat(accessGuard.getUnlockedChapterIds("user-1", "subject-1")).containsExactly("chapter-1");
        // Người dùng vẫn có thể xem nội dung chương và bài kiểm tra trên giao diện môn học
        accessGuard.requireVisibleChapter("chapter-2");
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
        when(unlocked.findByUserIdAndChapterIdIn(org.mockito.ArgumentMatchers.eq("user-1"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(
                        UserUnlockedChapter.create("user-1", "chapter-1"),
                        UserUnlockedChapter.create("user-1", "chapter-2")));

        assertThat(accessGuard.canAccessChapter("user-1", "chapter-2")).isTrue();
    }

    @Test
    void skippingAheadDoesNotUnlockSubsequentChapterWhenPrecedingGapExists() {
        JpaChapterSkillRepository requirements = mock(JpaChapterSkillRepository.class);
        JpaUserUnlockedChapterRepository unlocked = mock(JpaUserUnlockedChapterRepository.class);
        JpaUserSkillAbilityRepository abilities = mock(JpaUserSkillAbilityRepository.class);
        SubjectVisibilityGuard accessGuard = new SubjectVisibilityGuard(
                contentRepository, quizRepository, requirements, unlocked, abilities);
        authenticateAsUserPrincipal("user-1");

        Chapter ch1 = Chapter.builder().chapterId("ch-1").subjectId("subject-1").name("Chương 1").displayOrder(1).build();
        Chapter ch2 = Chapter.builder().chapterId("ch-2").subjectId("subject-1").name("Chương 2").displayOrder(2).build();
        Chapter ch3 = Chapter.builder().chapterId("ch-3").subjectId("subject-1").name("Chương 3").displayOrder(3).build();
        Chapter ch4 = Chapter.builder().chapterId("ch-4").subjectId("subject-1").name("Chương 4").displayOrder(4).build();
        Chapter ch5 = Chapter.builder().chapterId("ch-5").subjectId("subject-1").name("Chương 5").displayOrder(5).build();
        Chapter ch6 = Chapter.builder().chapterId("ch-6").subjectId("subject-1").name("Chương 6").displayOrder(6).build();

        when(contentRepository.findAllChapter("subject-1")).thenReturn(List.of(ch1, ch2, ch3, ch4, ch5, ch6));
        when(contentRepository.findChapterById("ch-5")).thenReturn(Optional.of(ch5));
        when(contentRepository.findChapterById("ch-6")).thenReturn(Optional.of(ch6));

        // ch-1, ch-2 yêu cầu skill-1 (0.5), ch-3 yêu cầu skill-2 (1.0), ch-5 yêu cầu skill-3 (0.8)
        when(requirements.findByChapterIdIn(List.of("ch-1", "ch-2", "ch-3", "ch-4", "ch-5", "ch-6")))
                .thenReturn(List.of(
                        ChapterSkill.create("ch-1", "skill-1", 0.5),
                        ChapterSkill.create("ch-2", "skill-1", 0.5),
                        ChapterSkill.create("ch-3", "skill-2", 1.0),
                        ChapterSkill.create("ch-5", "skill-3", 0.8)
                ));

        // User có skill-1: 0.8 (đủ ch1, ch2), skill-2: 0.1 (CHƯA ĐỦ ch3), nhưng skill-3: 1.5 (làm ch5 điểm cao)
        UserSkillAbility s1 = UserSkillAbility.create("user-1", "skill-1"); s1.updateAbility(0.8, 1.0);
        UserSkillAbility s2 = UserSkillAbility.create("user-1", "skill-2"); s2.updateAbility(0.1, 1.0);
        UserSkillAbility s3 = UserSkillAbility.create("user-1", "skill-3"); s3.updateAbility(1.5, 1.0);
        when(abilities.findByUserId("user-1")).thenReturn(List.of(s1, s2, s3));

        // Kiểm tra mở khóa:
        // ch-1: mở, ch-2: mở, ch-3: mở (vì ch1, ch2 pass)
        // Nhưng ch-3 không pass -> chuỗi đứt tại ch-3 -> ch-4, ch-5, ch-6 ĐỀU BỊ KHÓA
        assertThat(accessGuard.canAccessChapter("user-1", "ch-6")).isFalse();
        assertThat(accessGuard.canAccessChapter("user-1", "ch-5")).isFalse();
        assertThat(accessGuard.getUnlockedChapterIds("user-1", "subject-1"))
                .containsExactly("ch-1", "ch-2", "ch-3");
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

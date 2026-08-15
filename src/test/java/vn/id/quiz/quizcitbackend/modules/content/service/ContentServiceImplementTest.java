package vn.id.quiz.quizcitbackend.modules.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import vn.id.quiz.quizcitbackend.modules.chatbot.service.TheoryIndexingService;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaChapterSkillRepository;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaUserUnlockedChapterRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.SkillRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.persistence.JpaUserSkillAbilityRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;
import vn.id.quiz.quizcitbackend.shared.security.CustomUserPrincipal;

class ContentServiceImplementTest {
    private final ContentRepository content = mock(ContentRepository.class);
    private final ContentMapper mapper = mock(ContentMapper.class);
    private final QuizRepository quizzes = mock(QuizRepository.class);
    private final SubjectVisibilityGuard visibility = mock(SubjectVisibilityGuard.class);
    private final JpaChapterSkillRepository chapterSkills = mock(JpaChapterSkillRepository.class);
    private final JpaUserUnlockedChapterRepository unlocked = mock(JpaUserUnlockedChapterRepository.class);
    private final JpaUserSkillAbilityRepository abilities = mock(JpaUserSkillAbilityRepository.class);
    private final SkillRepository skills = mock(SkillRepository.class);
    private final TheoryIndexingService indexing = mock(TheoryIndexingService.class);
    private final ContentServiceImplement service = new ContentServiceImplement(
            content, mapper, quizzes, visibility, chapterSkills, unlocked, abilities, skills, indexing);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void chapterListingRequiresCourseAccess() {
        when(content.findAllChapter("subject-1")).thenReturn(List.of());

        assertThat(service.getAllChapters("subject-1")).isEmpty();

        verify(visibility).requireAccessibleSubject("subject-1");
        verify(visibility, never()).requireVisibleSubject("subject-1");
    }

    @Test
    void subjectReturnedByGuardIsNotLoadedAgain() {
        Subject subject = Subject.builder().subjectId("subject-1").courseCode("CT101")
                .name("Lập trình").visible(true).displayOrder(1).build();
        when(visibility.requireVisibleSubjectEntity("subject-1")).thenReturn(subject);

        service.getSubjectById("subject-1");

        verify(content, never()).findSubjectById("subject-1");
    }

    @Test
    void newlyAccessibleChaptersAreUnlockedInOneBulkCall() {
        Chapter first = Chapter.builder().chapterId("chapter-1").subjectId("subject-1")
                .name("Chương 1").displayOrder(1).build();
        Chapter second = Chapter.builder().chapterId("chapter-2").subjectId("subject-1")
                .name("Chương 2").displayOrder(2).build();
        var principal = new CustomUserPrincipal(
                "user-1", "student", "student@example.com", "USER", "ACTIVE");
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(principal, null));
        when(visibility.isAdministrator()).thenReturn(false);
        when(content.findAccessibleChapters("user-1")).thenReturn(List.of(first, second));
        when(chapterSkills.findByChapterIdIn(List.of("chapter-1", "chapter-2"))).thenReturn(List.of());
        when(skills.findByIds(java.util.Set.of())).thenReturn(List.of());
        when(unlocked.findByUserIdAndChapterIdIn("user-1", List.of("chapter-1", "chapter-2")))
                .thenReturn(List.of());
        when(abilities.findByUserId("user-1")).thenReturn(List.of());

        assertThat(service.getAllAccessibleChapters()).hasSize(2);
        verify(unlocked).insertAllIfAbsent("user-1", List.of("chapter-1", "chapter-2"));
    }
}

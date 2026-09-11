package vn.id.quiz.quizcitbackend.modules.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.ChapterResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.page.HomeSummaryResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.EnrolledSubjectResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.UserSkillAbility;
import vn.id.quiz.quizcitbackend.modules.progress.repository.ProgressRepository;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.repository.UserRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

class PageServiceImplementTest {

    private UserRepository userRepository;
    private ContentService contentService;
    private CourseEnrollmentService enrollmentService;
    private ContentRepository contentRepository;
    private ProgressRepository progressRepository;
    private PageServiceImplement pageService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        contentService = mock(ContentService.class);
        enrollmentService = mock(CourseEnrollmentService.class);
        contentRepository = mock(ContentRepository.class);
        progressRepository = mock(ProgressRepository.class);

        pageService = new PageServiceImplement(
                userRepository,
                contentService,
                enrollmentService,
                contentRepository,
                progressRepository
        );
    }

    @Test
    void getHomeSummaryThrowsUnauthorizedWhenUserIdBlank() {
        assertThatThrownBy(() -> pageService.getHomeSummary(""))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    void getHomeSummaryReturnsAggregatedDashboard() {
        String userId = "user-123";
        User user = User.builder()
                .userId(userId)
                .username("anhuan")
                .displayName("Nguyễn Văn An")
                .build();
        when(userRepository.findUserById(userId)).thenReturn(Optional.of(user));

        UserSkillAbility a1 = UserSkillAbility.builder().id("usa-1").userId(userId).skillId("s1").theta(1.8).build();
        when(progressRepository.findUserSkillAbilities(userId)).thenReturn(List.of(a1));
        when(progressRepository.getAverageAbility(userId)).thenReturn(1.8);

        EnrolledSubjectResponse enrolledSubject = new EnrolledSubjectResponse(
                "sub-1", "CS101", "Cơ sở lập trình", "Mô tả",
                ZonedDateTime.now(), 1, ZonedDateTime.now(), "https://img.example/cs101.png"
        );
        when(enrollmentService.getEnrolledSubjects(userId)).thenReturn(List.of(enrolledSubject));

        ChapterResponse ch1 = new ChapterResponse("ch-1", "sub-1", "Chương 1", ZonedDateTime.now(), 1, List.of(), true);
        ChapterResponse ch2 = new ChapterResponse("ch-2", "sub-1", "Chương 2", ZonedDateTime.now(), 2, List.of(), true);
        when(contentService.getAllAccessibleChapters()).thenReturn(List.of(ch1, ch2));

        Chapter dbCh1 = Chapter.builder().chapterId("ch-1").subjectId("sub-1").name("Chương 1").build();
        Chapter dbCh2 = Chapter.builder().chapterId("ch-2").subjectId("sub-1").name("Chương 2").build();
        Chapter dbCh3 = Chapter.builder().chapterId("ch-3").subjectId("sub-1").name("Chương 3").build();
        when(contentRepository.findAllChapters(List.of("sub-1"))).thenReturn(List.of(dbCh1, dbCh2, dbCh3));

        HomeSummaryResponse summary = pageService.getHomeSummary(userId);

        assertThat(summary).isNotNull();
        assertThat(summary.greeting().displayName()).isEqualTo("Nguyễn Văn An");
        assertThat(summary.greeting().firstName()).isEqualTo("An");
        assertThat(summary.greeting().period()).isIn("sáng", "chiều", "tối");

        assertThat(summary.abilityOverview().skillCount()).isEqualTo(1);
        assertThat(summary.abilityOverview().averageTheta()).isEqualTo(1.8);
        assertThat(summary.abilityOverview().abilityLevel()).isEqualTo("Giỏi");

        assertThat(summary.enrolledSubjects()).hasSize(1);
        assertThat(summary.enrolledSubjects().get(0).subjectId()).isEqualTo("sub-1");
        assertThat(summary.enrolledSubjects().get(0).unlockedChapterCount()).isEqualTo(2);
        assertThat(summary.enrolledSubjects().get(0).totalChapterCount()).isEqualTo(3);

        assertThat(summary.stats().totalEnrolledSubjects()).isEqualTo(1);
        assertThat(summary.stats().totalUnlockedChapters()).isEqualTo(2);
    }

    @Test
    void getHomeSummaryCatchesAbilityErrorGracefully() {
        String userId = "user-123";
        when(userRepository.findUserById(userId)).thenReturn(Optional.empty());
        when(progressRepository.findUserSkillAbilities(userId)).thenThrow(new RuntimeException("DB error"));
        when(enrollmentService.getEnrolledSubjects(userId)).thenReturn(List.of());
        when(contentService.getAllAccessibleChapters()).thenReturn(List.of());

        HomeSummaryResponse summary = pageService.getHomeSummary(userId);

        assertThat(summary).isNotNull();
        assertThat(summary.abilityOverview().averageTheta()).isNull();
        assertThat(summary.abilityOverview().skillCount()).isEqualTo(0);
        assertThat(summary.abilityOverview().abilityLevel()).isNull();
        assertThat(summary.enrolledSubjects()).isEmpty();
        assertThat(summary.stats().totalEnrolledSubjects()).isEqualTo(0);
        assertThat(summary.stats().totalUnlockedChapters()).isEqualTo(0);
    }
}

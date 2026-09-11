package vn.id.quiz.quizcitbackend.modules.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import vn.id.quiz.quizcitbackend.modules.chatbot.service.TheoryIndexingService;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Theory;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaChapterSkillRepository;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaUserUnlockedChapterRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.SkillRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.persistence.JpaUserSkillAbilityRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
import vn.id.quiz.quizcitbackend.shared.security.CustomUserPrincipal;

import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaUserCourseEnrollmentRepository;

class ContentServiceImplementTest {
    private final ContentRepository content = mock(ContentRepository.class);
    private final ContentMapper mapper = mock(ContentMapper.class);
    private final QuizRepository quizzes = mock(QuizRepository.class);
    private final SubjectVisibilityGuard visibility = mock(SubjectVisibilityGuard.class);
    private final JpaChapterSkillRepository chapterSkills = mock(JpaChapterSkillRepository.class);
    private final JpaUserUnlockedChapterRepository unlocked = mock(JpaUserUnlockedChapterRepository.class);
    private final JpaUserSkillAbilityRepository abilities = mock(JpaUserSkillAbilityRepository.class);
    private final JpaUserCourseEnrollmentRepository enrollments = mock(JpaUserCourseEnrollmentRepository.class);
    private final SkillRepository skills = mock(SkillRepository.class);
    private final TheoryIndexingService indexing = mock(TheoryIndexingService.class);
    private final ContentServiceImplement service = new ContentServiceImplement(
            content, mapper, quizzes, visibility, chapterSkills, unlocked, abilities, enrollments, skills, indexing);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void chapterListingRequiresCourseAccess() {
        Subject subject = Subject.builder().subjectId("subject-1").courseCode("CT101")
                .name("Lập trình").visible(true).displayOrder(1).build();
        when(visibility.requireVisibleSubjectEntity("subject-1")).thenReturn(subject);
        when(content.findAllChapter("subject-1")).thenReturn(List.of());

        assertThat(service.getAllChapters("subject-1")).isEmpty();

        verify(visibility).requireAccessibleSubject("subject-1");
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
        assertThat(service.getAllAccessibleChapters()).hasSize(2);
        verify(unlocked).insertAllIfAbsent("user-1", List.of("chapter-1", "chapter-2"));
    }

    @Test
    void sequentialUnlockChainBlocksChapter2WhenChapter1SkillsNotMet() {
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
        when(chapterSkills.findByChapterIdIn(List.of("chapter-1", "chapter-2"))).thenReturn(List.of(
                vn.id.quiz.quizcitbackend.modules.content.presentation.model.ChapterSkill.create("chapter-1", "skill-1", 1.0)
        ));
        when(skills.findByIds(java.util.Set.of("skill-1"))).thenReturn(List.of(
                vn.id.quiz.quizcitbackend.modules.progress.presentation.model.Skill.builder()
                        .skillId("skill-1").name("Skill 1").description("Desc").build()
        ));
        // User has no theta or theta < 1.0
        when(abilities.findByUserId("user-1")).thenReturn(List.of());
        when(unlocked.findByUserIdAndChapterIdIn("user-1", List.of("chapter-1", "chapter-2"))).thenReturn(List.of());

        var result = service.getAllAccessibleChapters();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).chapterId()).isEqualTo("chapter-1");
        verify(unlocked).insertAllIfAbsent("user-1", List.of("chapter-1"));
    }

    @Test
    void getAllChaptersReturnsAllChaptersWithUnlockStatus() {
        String subjectId = "subject-1";
        Chapter first = Chapter.builder().chapterId("chapter-1").subjectId(subjectId)
                .name("Chương 1").displayOrder(1).build();
        Chapter second = Chapter.builder().chapterId("chapter-2").subjectId(subjectId)
                .name("Chương 2").displayOrder(2).build();
        var principal = new CustomUserPrincipal(
                "user-1", "student", "student@example.com", "USER", "ACTIVE");
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(principal, null));
        when(visibility.isAdministrator()).thenReturn(false);
        when(visibility.requireVisibleSubjectEntity(subjectId)).thenReturn(
                vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject.builder()
                        .subjectId(subjectId).courseCode("CT101").name("Subject").build()
        );
        when(content.findAllChapter(subjectId)).thenReturn(List.of(first, second));
        when(chapterSkills.findByChapterIdIn(List.of("chapter-1", "chapter-2"))).thenReturn(List.of(
                vn.id.quiz.quizcitbackend.modules.content.presentation.model.ChapterSkill.create("chapter-1", "skill-1", 1.0)
        ));
        when(skills.findByIds(java.util.Set.of("skill-1"))).thenReturn(List.of(
                vn.id.quiz.quizcitbackend.modules.progress.presentation.model.Skill.builder()
                        .skillId("skill-1").name("Skill 1").description("Desc").build()
        ));
        when(abilities.findByUserId("user-1")).thenReturn(List.of());
        when(unlocked.findByUserIdAndChapterIdIn("user-1", List.of("chapter-1", "chapter-2"))).thenReturn(List.of());

        var allChapters = service.getAllChapters(subjectId);
        // Returns ALL chapters
        assertThat(allChapters).hasSize(2);
        assertThat(allChapters.get(0).chapterId()).isEqualTo("chapter-1");
        assertThat(allChapters.get(0).unlocked()).isTrue();
        assertThat(allChapters.get(0).skills()).hasSize(1);
        assertThat(allChapters.get(0).skills().get(0).currentTheta()).isNull();
        assertThat(allChapters.get(1).chapterId()).isEqualTo("chapter-2");
        assertThat(allChapters.get(1).unlocked()).isFalse();
    }

    @Test
    void getAllChaptersReturnsCurrentThetaWhenAbilityExists() {
        String subjectId = "subject-1";
        Chapter first = Chapter.builder().chapterId("chapter-1").subjectId(subjectId)
                .name("Chương 1").displayOrder(1).build();
        var principal = new CustomUserPrincipal(
                "user-1", "student", "student@example.com", "USER", "ACTIVE");
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(principal, null));
        when(visibility.isAdministrator()).thenReturn(false);
        when(visibility.requireVisibleSubjectEntity(subjectId)).thenReturn(
                vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject.builder()
                        .subjectId(subjectId).courseCode("CT101").name("Subject").build()
        );
        when(content.findAllChapter(subjectId)).thenReturn(List.of(first));
        when(chapterSkills.findByChapterIdIn(List.of("chapter-1"))).thenReturn(List.of(
                vn.id.quiz.quizcitbackend.modules.content.presentation.model.ChapterSkill.create("chapter-1", "skill-1", 1.0)
        ));
        when(skills.findByIds(java.util.Set.of("skill-1"))).thenReturn(List.of(
                vn.id.quiz.quizcitbackend.modules.progress.presentation.model.Skill.builder()
                        .skillId("skill-1").name("Skill 1").description("Desc").build()
        ));
        var ability = vn.id.quiz.quizcitbackend.modules.progress.presentation.model.UserSkillAbility.create("user-1", "skill-1");
        ability.updateAbility(1.5);
        when(abilities.findByUserId("user-1")).thenReturn(List.of(ability));
        when(unlocked.findByUserIdAndChapterIdIn("user-1", List.of("chapter-1"))).thenReturn(List.of());

        var allChapters = service.getAllChapters(subjectId);
        assertThat(allChapters).hasSize(1);
        assertThat(allChapters.get(0).skills()).hasSize(1);
        assertThat(allChapters.get(0).skills().get(0).requiredTheta()).isEqualTo(1.0);
        assertThat(allChapters.get(0).skills().get(0).currentTheta()).isEqualTo(1.5);
    }

    @Test
    void reorderChaptersRecalculatesUnlockedChaptersForEnrolledUsers() {
        String subjectId = "subject-1";
        Chapter first = Chapter.builder().chapterId("chapter-1").subjectId(subjectId)
                .name("Chương 1").displayOrder(1).build();
        Chapter second = Chapter.builder().chapterId("chapter-2").subjectId(subjectId)
                .name("Chương 2").displayOrder(2).build();

        when(content.existsBySubjectId(subjectId)).thenReturn(true);
        when(content.findChaptersByIds(List.of("chapter-2", "chapter-1"))).thenReturn(List.of(first, second));
        when(content.findAllChapter(subjectId)).thenReturn(List.of(second, first));
        when(enrollments.findUserIdsBySubjectId(subjectId)).thenReturn(List.of("user-1"));
        when(chapterSkills.findByChapterIdIn(List.of("chapter-2", "chapter-1"))).thenReturn(List.of());
        when(abilities.findByUserId("user-1")).thenReturn(List.of());

        vn.id.quiz.quizcitbackend.shared.dto.ReorderRequest req = new vn.id.quiz.quizcitbackend.shared.dto.ReorderRequest(
                List.of(
                        new vn.id.quiz.quizcitbackend.shared.dto.ReorderItemRequest("chapter-2", 1),
                        new vn.id.quiz.quizcitbackend.shared.dto.ReorderItemRequest("chapter-1", 2)
                )
        );

        service.reorderChapters(subjectId, req);

        verify(unlocked).deleteByChapterIdIn(List.of("chapter-2", "chapter-1"));
        verify(unlocked).insertAllIfAbsent("user-1", List.of("chapter-2", "chapter-1"));
    }

    @Test
    void deleteSubjectShouldReassignQuestionsAndCascadeDeleteChaptersTheoriesQuizzes() {
        String subjectId = "subject-1";
        Subject subject = Subject.builder().subjectId(subjectId).courseCode("CT101")
                .name("Lập trình").displayOrder(1).build();
        Chapter chapter = Chapter.builder().chapterId("chapter-1").subjectId(subjectId)
                .name("Chương 1").displayOrder(1).build();
        Theory theory = Theory.builder().theoryId("theory-1").chapterId("chapter-1")
                .content("Lý thuyết").contentFormat("plain").build();
        Quiz quiz = Quiz.builder().quizId("quiz-1").subjectId(subjectId).chapterId("chapter-1")
                .name("Quiz 1").build();

        Subject bankSubject = Subject.builder().subjectId("bank-sub").courseCode("QUESTION_BANK")
                .name("Ngân hàng câu hỏi").displayOrder(99).build();
        Chapter bankChapter = Chapter.builder().chapterId("bank-chap").subjectId("bank-sub")
                .name("Ngân hàng câu hỏi").displayOrder(1).build();

        when(content.findSubjectById(subjectId)).thenReturn(Optional.of(subject));
        when(content.getOrCreateQuestionBankSubject()).thenReturn(bankSubject);
        when(content.getOrCreateQuestionBankChapter("bank-sub")).thenReturn(bankChapter);
        when(content.findAllChapter(subjectId)).thenReturn(List.of(chapter));
        when(content.findAllTheory("chapter-1")).thenReturn(List.of(theory));
        when(quizzes.findQuizzesBySubjectId(subjectId)).thenReturn(List.of(quiz));

        service.deleteSubject(subjectId);

        verify(quizzes).reassignQuestionsBySubjectId(subjectId, "bank-sub", "bank-chap");
        verify(quizzes).reassignQuestionsByChapterIds(List.of("chapter-1"), "bank-sub", "bank-chap");
        verify(content).deleteTheoryById("theory-1");
        verify(indexing).delete("theory-1");
        verify(chapterSkills).deleteByChapterId("chapter-1");
        verify(unlocked).deleteByChapterId("chapter-1");
        verify(content).deleteChapterById("chapter-1");
        verify(quizzes).deleteQuizQuestionsByQuizId("quiz-1");
        verify(quizzes).deleteQuizById("quiz-1");
        verify(content).deleteSubjectById(subjectId);
    }

    @Test
    void deleteSubjectShouldThrowWhenAttemptingToDeleteDefaultQuestionBank() {
        Subject bankSubject = Subject.builder().subjectId("bank-sub").courseCode("QUESTION_BANK")
                .name("Ngân hàng câu hỏi").displayOrder(99).build();
        when(content.findSubjectById("bank-sub")).thenReturn(Optional.of(bankSubject));

        assertThatThrownBy(() -> service.deleteSubject("bank-sub"))
                .isInstanceOf(AppException.class)
                .hasMessage("Không thể xóa Ngân hàng câu hỏi mặc định");
    }

    @Test
    void deleteSubjectShouldThrowWhenSubjectNotFound() {
        when(content.findSubjectById("non-existing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteSubject("non-existing"))
                .isInstanceOf(AppException.class);
    }

    @Test
    void deleteChapterShouldReassignQuestionsAndCascadeDeleteTheoriesQuizzes() {
        String chapterId = "chapter-1";
        Chapter chapter = Chapter.builder().chapterId(chapterId).subjectId("subject-1")
                .name("Chương 1").build();
        Subject subject = Subject.builder().subjectId("subject-1").courseCode("CT101")
                .name("Lập trình").build();
        Theory theory = Theory.builder().theoryId("theory-1").chapterId(chapterId)
                .content("Lý thuyết").contentFormat("plain").build();
        Quiz quiz = Quiz.builder().quizId("quiz-1").chapterId(chapterId).name("Quiz 1").build();

        Subject bankSubject = Subject.builder().subjectId("bank-sub").courseCode("QUESTION_BANK")
                .name("Ngân hàng câu hỏi").displayOrder(99).build();
        Chapter bankChapter = Chapter.builder().chapterId("bank-chap").subjectId("bank-sub")
                .name("Ngân hàng câu hỏi").displayOrder(1).build();

        when(content.findChapterById(chapterId)).thenReturn(Optional.of(chapter));
        when(content.findSubjectById("subject-1")).thenReturn(Optional.of(subject));
        when(content.getOrCreateQuestionBankSubject()).thenReturn(bankSubject);
        when(content.getOrCreateQuestionBankChapter("bank-sub")).thenReturn(bankChapter);
        when(content.findAllTheory(chapterId)).thenReturn(List.of(theory));
        when(quizzes.findQuizzesByChapterId(chapterId)).thenReturn(List.of(quiz));

        service.deleteChapter(chapterId);

        verify(quizzes).reassignQuestionsByChapterId(chapterId, "bank-sub", "bank-chap");
        verify(content).deleteTheoryById("theory-1");
        verify(indexing).delete("theory-1");
        verify(chapterSkills).deleteByChapterId(chapterId);
        verify(unlocked).deleteByChapterId(chapterId);
        verify(quizzes).deleteQuizQuestionsByQuizId("quiz-1");
        verify(quizzes).deleteQuizById("quiz-1");
        verify(content).deleteChapterById(chapterId);
    }

    @Test
    void deleteChapterShouldThrowWhenAttemptingToDeleteDefaultQuestionBankChapter() {
        Chapter bankChapter = Chapter.builder().chapterId("bank-chap").subjectId("bank-sub")
                .name("Ngân hàng câu hỏi").build();
        Subject bankSubject = Subject.builder().subjectId("bank-sub").courseCode("QUESTION_BANK")
                .name("Ngân hàng câu hỏi").build();

        when(content.findChapterById("bank-chap")).thenReturn(Optional.of(bankChapter));
        when(content.findSubjectById("bank-sub")).thenReturn(Optional.of(bankSubject));

        assertThatThrownBy(() -> service.deleteChapter("bank-chap"))
                .isInstanceOf(AppException.class)
                .hasMessage("Không thể xóa Ngân hàng câu hỏi mặc định");
    }

    @Test
    void deleteChapterShouldThrowWhenChapterNotFound() {
        when(content.findChapterById("non-existing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteChapter("non-existing"))
                .isInstanceOf(AppException.class);
    }

    @Test
    void updateSubjectShouldThrowWhenAttemptingToUpdateDefaultQuestionBank() {
        Subject bankSubject = Subject.builder().subjectId("bank-sub").courseCode("QUESTION_BANK")
                .name("Ngân hàng câu hỏi").build();
        when(content.findSubjectById("bank-sub")).thenReturn(Optional.of(bankSubject));

        var req = new vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.UpdateSubjectRequest(
                "QUESTION_BANK", "New Name", "Desc", true, 1, null);
        assertThatThrownBy(() -> service.updateSubject("bank-sub", req))
                .isInstanceOf(AppException.class)
                .hasMessage("Không thể chỉnh sửa Ngân hàng câu hỏi mặc định");
    }

    @Test
    void updateChapterShouldThrowWhenAttemptingToUpdateDefaultQuestionBankChapter() {
        Chapter bankChapter = Chapter.builder().chapterId("bank-chap").subjectId("bank-sub")
                .name("Ngân hàng câu hỏi").build();
        Subject bankSubject = Subject.builder().subjectId("bank-sub").courseCode("QUESTION_BANK")
                .name("Ngân hàng câu hỏi").build();
        when(content.findChapterById("bank-chap")).thenReturn(Optional.of(bankChapter));
        when(content.findSubjectById("bank-sub")).thenReturn(Optional.of(bankSubject));

        var req = new vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.UpdateChapterRequest(
                "New Chapter Name", 1, List.of());
        assertThatThrownBy(() -> service.updateChapter("bank-chap", req))
                .isInstanceOf(AppException.class)
                .hasMessage("Không thể chỉnh sửa Ngân hàng câu hỏi mặc định");
    }

    @Test
    void updateChapterShouldClampDisplayOrderTo1WhenNegativeAndSwapWithFirstChapter() {
        Subject normalSubject = Subject.builder().subjectId("sub-1").courseCode("CS101").name("Computer Science").build();
        Chapter ch1 = Chapter.builder().chapterId("ch-1").subjectId("sub-1").name("Chapter 1").displayOrder(1).build();
        Chapter ch2 = Chapter.builder().chapterId("ch-2").subjectId("sub-1").name("Chapter 2").displayOrder(2).build();
        Chapter ch3 = Chapter.builder().chapterId("ch-3").subjectId("sub-1").name("Chapter 3").displayOrder(3).build();

        when(content.findChapterById("ch-3")).thenReturn(Optional.of(ch3));
        when(content.findSubjectById("sub-1")).thenReturn(Optional.of(normalSubject));
        when(content.findAllChapter("sub-1")).thenReturn(List.of(ch1, ch2, ch3));

        var req = new vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.UpdateChapterRequest(
                "Chapter 3 Updated", -5, List.of());
        service.updateChapter("ch-3", req);

        // ch-3 displayOrder was -5 -> clamped to 1, swapped with ch-1 (which gets ch-3's old order = 3)
        assertThat(ch3.getDisplayOrder()).isEqualTo(1);
        assertThat(ch3.getName()).isEqualTo("Chapter 3 Updated");
        assertThat(ch1.getDisplayOrder()).isEqualTo(3);
        verify(content).save(ch1);
        verify(content).save(ch3);
    }

    @Test
    void updateChapterShouldClampDisplayOrderToMaxWhenGreaterThanTotalAndSwapWithLastChapter() {
        Subject normalSubject = Subject.builder().subjectId("sub-1").courseCode("CS101").name("Computer Science").build();
        Chapter ch1 = Chapter.builder().chapterId("ch-1").subjectId("sub-1").name("Chapter 1").displayOrder(1).build();
        Chapter ch2 = Chapter.builder().chapterId("ch-2").subjectId("sub-1").name("Chapter 2").displayOrder(2).build();
        Chapter ch3 = Chapter.builder().chapterId("ch-3").subjectId("sub-1").name("Chapter 3").displayOrder(3).build();

        when(content.findChapterById("ch-1")).thenReturn(Optional.of(ch1));
        when(content.findSubjectById("sub-1")).thenReturn(Optional.of(normalSubject));
        when(content.findAllChapter("sub-1")).thenReturn(List.of(ch1, ch2, ch3));

        var req = new vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.UpdateChapterRequest(
                "Chapter 1 Updated", 999, List.of());
        service.updateChapter("ch-1", req);

        // ch-1 displayOrder was 999 -> clamped to 3 (total chapters = 3), swapped with ch-3 (which gets ch-1's old order = 1)
        assertThat(ch1.getDisplayOrder()).isEqualTo(3);
        assertThat(ch1.getName()).isEqualTo("Chapter 1 Updated");
        assertThat(ch3.getDisplayOrder()).isEqualTo(1);
        verify(content).save(ch3);
        verify(content).save(ch1);
    }

    @Test
    void updateChapterShouldSwapWhenValidOrderOccupiedByAnotherChapter() {
        Subject normalSubject = Subject.builder().subjectId("sub-1").courseCode("CS101").name("Computer Science").build();
        Chapter ch1 = Chapter.builder().chapterId("ch-1").subjectId("sub-1").name("Chapter 1").displayOrder(1).build();
        Chapter ch2 = Chapter.builder().chapterId("ch-2").subjectId("sub-1").name("Chapter 2").displayOrder(2).build();

        when(content.findChapterById("ch-1")).thenReturn(Optional.of(ch1));
        when(content.findSubjectById("sub-1")).thenReturn(Optional.of(normalSubject));
        when(content.findAllChapter("sub-1")).thenReturn(List.of(ch1, ch2));

        var req = new vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.UpdateChapterRequest(
                "Chapter 1 Renamed", 2, List.of());
        service.updateChapter("ch-1", req);

        assertThat(ch1.getDisplayOrder()).isEqualTo(2);
        assertThat(ch2.getDisplayOrder()).isEqualTo(1);
        verify(content).save(ch2);
        verify(content).save(ch1);
    }

    @Test
    void addChapterShouldThrowWhenAttemptingToAddToQuestionBank() {
        Subject bankSubject = Subject.builder().subjectId("bank-sub").courseCode("QUESTION_BANK")
                .name("Ngân hàng câu hỏi").build();
        when(content.findSubjectById("bank-sub")).thenReturn(Optional.of(bankSubject));

        var req = new vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.CreateChapterRequest(
                "New Chapter", 1, List.of());
        assertThatThrownBy(() -> service.addChapter("bank-sub", req))
                .isInstanceOf(AppException.class)
                .hasMessage("Không thể thêm chương vào Ngân hàng câu hỏi mặc định");
    }

    @Test
    void createSubjectWithImageUrlShouldPersistAndReturn() {
        var req = new vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.CreateSubjectRequest(
                "CT102", "Cấu trúc dữ liệu", "Mô tả", 1, "https://example.com/ds.png");
        when(content.existsByCourseCode("CT102")).thenReturn(false);
        when(content.nextSubjectDisplayOrder()).thenReturn(1);
        when(content.save(org.mockito.ArgumentMatchers.any(Subject.class))).thenAnswer(i -> i.getArgument(0));
        when(mapper.toSubjectResponse(org.mockito.ArgumentMatchers.any(Subject.class))).thenAnswer(i -> {
            Subject s = i.getArgument(0);
            return new vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.SubjectResponse(
                    s.getSubjectId(), s.getCourseCode(), s.getName(), s.getDescription(), s.isVisible(), s.getCreatedAt(), s.getDisplayOrder(), s.getImageUrl());
        });

        var res = service.createSubject(req);
        assertThat(res.imageUrl()).isEqualTo("https://example.com/ds.png");
        assertThat(res.courseCode()).isEqualTo("CT102");
    }

    @Test
    void updateSubjectWithImageUrlShouldPersist() {
        Subject existing = Subject.builder().subjectId("sub-1").courseCode("CT102")
                .name("Cấu trúc dữ liệu").displayOrder(1).visible(true).build();
        when(content.findSubjectById("sub-1")).thenReturn(Optional.of(existing));

        var req = new vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.UpdateSubjectRequest(
                "CT102", "Cấu trúc dữ liệu và giải thuật", "Mô tả cập nhật", true, 1, "https://example.com/updated.png");

        service.updateSubject("sub-1", req);
        verify(content).save(existing);
        assertThat(existing.getImageUrl()).isEqualTo("https://example.com/updated.png");
        assertThat(existing.getName()).isEqualTo("Cấu trúc dữ liệu và giải thuật");
    }

    @Test
    void getChaptersSummaryShouldReturnSummarizedCounts() {
        Subject subject = Subject.builder().subjectId("sub-1").courseCode("CT101")
                .name("Lập trình C").visible(true).displayOrder(1).build();
        Chapter chapter = Chapter.builder().chapterId("ch-1").subjectId("sub-1")
                .name("Nhập môn C").displayOrder(1).build();

        when(visibility.requireVisibleSubjectEntity("sub-1")).thenReturn(subject);
        when(content.findAllChapter("sub-1")).thenReturn(List.of(chapter));
        when(quizzes.countQuestionsByChapterIds(List.of("ch-1"))).thenReturn(Map.of("ch-1", 15L));
        when(content.countTheoriesByChapterIds(List.of("ch-1"))).thenReturn(Map.of("ch-1", 3L));
        when(chapterSkills.findByChapterIdIn(List.of("ch-1"))).thenReturn(List.of());
        when(unlocked.findByUserIdAndChapterIdIn(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(List.of());

        var summaries = service.getChaptersSummary("sub-1");

        assertThat(summaries).hasSize(1);
        assertThat(summaries.get(0).chapterId()).isEqualTo("ch-1");
        assertThat(summaries.get(0).questionCount()).isEqualTo(15L);
        assertThat(summaries.get(0).theoryCount()).isEqualTo(3L);
        assertThat(summaries.get(0).unlocked()).isTrue();
    }

    @Test
    void getAllFlatChaptersShouldDelegateToRepository() {
        var flatChapter = new vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.FlatChapterResponse(
                "ch-1", "Chương 1", "sub-1", "Môn 1", 1);
        when(content.findAllFlatChapters()).thenReturn(List.of(flatChapter));

        var results = service.getAllFlatChapters();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).chapterId()).isEqualTo("ch-1");
        assertThat(results.get(0).chapterName()).isEqualTo("Chương 1");
        assertThat(results.get(0).subjectName()).isEqualTo("Môn 1");
    }

    @Test
    void getChapterEditContextShouldReturnFullEditingContext() {
        String chapterId = "ch-1";
        String subjectId = "sub-1";
        Subject subject = Subject.builder().subjectId(subjectId).courseCode("CS101")
                .name("Computer Science").displayOrder(1).build();
        Chapter chapter = Chapter.builder().chapterId(chapterId).subjectId(subjectId)
                .name("Introduction").displayOrder(1).build();
        Chapter sibling = Chapter.builder().chapterId("ch-2").subjectId(subjectId)
                .name("Data Types").displayOrder(2).build();
        Theory theory = Theory.builder().theoryId("th-1").chapterId(chapterId)
                .content("Content").contentFormat("plain").build();
        var skill = vn.id.quiz.quizcitbackend.modules.progress.presentation.model.Skill.builder()
                .skillId("sk-1").name("Variables").description("Desc").build();

        when(visibility.requireVisibleChapterEntity(chapterId)).thenReturn(chapter);
        when(content.findSubjectById(subjectId)).thenReturn(Optional.of(subject));
        when(content.findAllChapter(subjectId)).thenReturn(List.of(chapter, sibling));
        when(skills.findAll()).thenReturn(List.of(skill));
        when(content.findAllTheory(chapterId)).thenReturn(List.of(theory));
        when(chapterSkills.findByChapterId(chapterId)).thenReturn(List.of());
        when(abilities.findByUserId(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());
        when(mapper.toTheoryResponseList(List.of(theory))).thenReturn(List.of(
                new vn.id.quiz.quizcitbackend.modules.content.presentation.dto.theory.TheoryResponse(
                        "th-1", chapterId, "Content", "plain", null)));

        var context = service.getChapterEditContext(chapterId);

        assertThat(context.chapter().chapterId()).isEqualTo(chapterId);
        assertThat(context.subjectId()).isEqualTo(subjectId);
        assertThat(context.subjectName()).isEqualTo("Computer Science");
        assertThat(context.siblingChaptersCount()).isEqualTo(2);
        assertThat(context.siblingChapters()).hasSize(2);
        assertThat(context.availableSkills()).hasSize(1);
        assertThat(context.availableSkills().get(0).getSkillId()).isEqualTo("sk-1");
        assertThat(context.theories()).hasSize(1);
        assertThat(context.theories().get(0).theoryId()).isEqualTo("th-1");
    }
}

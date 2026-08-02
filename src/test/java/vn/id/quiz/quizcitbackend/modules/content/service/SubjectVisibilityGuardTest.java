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
    void administratorCanAccessQuizFromHiddenSubjectOutsidePreviewMode() {
        authenticateAs("ROLE_ADMINISTRATOR");
        stubQuizWithSubjectVisibility(false);

        guard.requireVisibleQuiz("quiz-1");
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

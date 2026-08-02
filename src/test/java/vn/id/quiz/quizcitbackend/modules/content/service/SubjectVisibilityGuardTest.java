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
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SubjectVisibilityGuardTest {

    private final SubjectVisibilityGuard guard = new SubjectVisibilityGuard(
            mock(ContentRepository.class),
            mock(QuizRepository.class));

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

    private void authenticateAs(String authority) {
        var authentication = new TestingAuthenticationToken(
                "test-user",
                null,
                List.of(new SimpleGrantedAuthority(authority)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}

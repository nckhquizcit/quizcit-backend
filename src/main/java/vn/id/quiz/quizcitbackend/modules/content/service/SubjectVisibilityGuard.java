package vn.id.quiz.quizcitbackend.modules.content.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Theory;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

@Component
@RequiredArgsConstructor
public class SubjectVisibilityGuard {
    public static final String VIEW_AS_HEADER = "X-QuizCIT-View-As";
    public static final String USER_VIEW = "USER";

    private final ContentRepository contentRepository;
    private final QuizRepository quizRepository;

    public boolean isAdministrator() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdministrator = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMINISTRATOR".equals(authority.getAuthority()));
        return isAdministrator && !isUserViewRequested();
    }

    private boolean isUserViewRequested() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return false;
        }
        return USER_VIEW.equalsIgnoreCase(attributes.getRequest().getHeader(VIEW_AS_HEADER));
    }

    public void requireVisibleSubject(String subjectId) {
        var subject = contentRepository.findSubjectById(subjectId)
                .orElseThrow(() -> new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND));
        if (!isAdministrator() && !subject.isVisible()) {
            // Return 404 so a hidden subject cannot be enumerated by ordinary users.
            throw new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
    }

    public void requireVisibleChapter(String chapterId) {
        Chapter chapter = contentRepository.findChapterById(chapterId)
                .orElseThrow(() -> new AppException(ErrorMessages.CHAPTER_NOT_FOUND, HttpStatus.NOT_FOUND));
        requireVisibleSubject(chapter.getSubjectId());
    }

    public void requireVisibleTheory(String theoryId) {
        Theory theory = contentRepository.findTheoryById(theoryId)
                .orElseThrow(() -> new AppException("Không tìm thấy nội dung lý thuyết được yêu cầu", HttpStatus.NOT_FOUND));
        requireVisibleChapter(theory.getChapterId());
    }

    public void requireVisibleQuiz(String quizId) {
        Quiz quiz = quizRepository.findQuizById(quizId)
                .orElseThrow(() -> new AppException(ErrorMessages.QUIZ_NOT_FOUND, HttpStatus.NOT_FOUND));
        requireVisibleSubject(quiz.getSubjectId());
    }
}

package vn.id.quiz.quizcitbackend.modules.chatbot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.id.quiz.quizcitbackend.modules.ai.dto.ChatRequest;
import vn.id.quiz.quizcitbackend.modules.ai.service.AIService;
import vn.id.quiz.quizcitbackend.modules.chatbot.presentation.dto.AskChatbotRequest;
import vn.id.quiz.quizcitbackend.modules.chatbot.presentation.dto.ChatbotAnswerResponse;
import vn.id.quiz.quizcitbackend.modules.chatbot.repository.TheoryChunkMatch;
import vn.id.quiz.quizcitbackend.modules.chatbot.repository.TheoryChunkRepository;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.UserUnlockedChapter;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaUserUnlockedChapterRepository;
import vn.id.quiz.quizcitbackend.modules.content.service.SubjectVisibilityGuard;

class ChatbotServiceImplementTest {

    private ContentRepository contentRepository;
    private JpaUserUnlockedChapterRepository unlockedChapterRepository;
    private SubjectVisibilityGuard visibilityGuard;
    private TheoryChunkRepository theoryChunkRepository;
    private AIService aiService;
    private ChatbotServiceImplement chatbotService;

    private static final String USER_ID = "user-123";
    private static final String SUBJECT_ID = "sub-ct101";

    @BeforeEach
    void setUp() {
        contentRepository = mock(ContentRepository.class);
        unlockedChapterRepository = mock(JpaUserUnlockedChapterRepository.class);
        visibilityGuard = mock(SubjectVisibilityGuard.class);
        theoryChunkRepository = mock(TheoryChunkRepository.class);
        aiService = mock(AIService.class);

        chatbotService = new ChatbotServiceImplement(
                contentRepository,
                unlockedChapterRepository,
                visibilityGuard,
                theoryChunkRepository,
                aiService);
    }

    @Test
    void askWithUnlockedChapterShouldAnswerPromptly() {
        String chapterId = "chp-2";
        Chapter ch1 = Chapter.builder().chapterId("chp-1").subjectId(SUBJECT_ID).name("Chương 1").displayOrder(1).build();
        Chapter ch2 = Chapter.builder().chapterId("chp-2").subjectId(SUBJECT_ID).name("Chương 2").displayOrder(2).build();

        when(contentRepository.findChapterById(chapterId)).thenReturn(Optional.of(ch2));
        when(visibilityGuard.canAccessChapter(USER_ID, chapterId)).thenReturn(true);
        when(contentRepository.findAllChapter(SUBJECT_ID)).thenReturn(List.of(ch1, ch2));
        when(unlockedChapterRepository.findByUserIdAndChapterIdIn(eq(USER_ID), any()))
                .thenReturn(List.of(UserUnlockedChapter.create(USER_ID, "chp-1"), UserUnlockedChapter.create(USER_ID, "chp-2")));

        float[] vector = new float[] {0.1f, 0.2f};
        when(aiService.embedTexts(List.of("malloc trong C hoạt động như thế nào?"))).thenReturn(List.of(vector));
        when(theoryChunkRepository.findTopK(eq(vector), any(), anyInt()))
                .thenReturn(List.of(new TheoryChunkMatch("chk-1", "thy-1", "chp-2", "malloc cấp phát bộ nhớ", 0.1)));
        when(aiService.chat(any(ChatRequest.class))).thenReturn("malloc cấp phát vùng nhớ động trên heap.");

        AskChatbotRequest request = new AskChatbotRequest(SUBJECT_ID, chapterId, "malloc trong C hoạt động như thế nào?", List.of());
        ChatbotAnswerResponse response = chatbotService.ask(USER_ID, request);

        assertThat(response.answer()).isEqualTo("malloc cấp phát vùng nhớ động trên heap.");
        assertThat(response.hasUnlockedContent()).isTrue();
        assertThat(response.sources()).hasSize(1);
        assertThat(response.sources().get(0).chapterId()).isEqualTo("chp-2");
    }

    @Test
    void askWithSubsequentChapterWhenPrecedingSkillsNotMetShouldRejectWithoutCallingAI() {
        String chapterId = "chp-3";
        Chapter ch3 = Chapter.builder().chapterId(chapterId).subjectId(SUBJECT_ID).name("Chương 3").displayOrder(3).build();

        when(contentRepository.findChapterById(chapterId)).thenReturn(Optional.of(ch3));
        // Preceding chapters theta requirement not met:
        when(visibilityGuard.canAccessChapter(USER_ID, chapterId)).thenReturn(false);

        AskChatbotRequest request = new AskChatbotRequest(SUBJECT_ID, chapterId, "Câu hỏi về chương 3", List.of());
        ChatbotAnswerResponse response = chatbotService.ask(USER_ID, request);

        assertThat(response.hasUnlockedContent()).isFalse();
        assertThat(response.answer()).contains("chưa mở khóa chương này do chưa đạt yêu cầu");
        assertThat(response.sources()).isEmpty();

        // AI service should never be called
        verify(aiService, never()).embedTexts(any());
        verify(aiService, never()).chat(any());
    }

    @Test
    void askWhenChapterDoesNotExistInSubjectShouldReject() {
        String chapterId = "chp-diff-subject";
        Chapter otherSubjectChapter = Chapter.builder().chapterId(chapterId).subjectId("other-subject").name("Chương khác").build();

        when(contentRepository.findChapterById(chapterId)).thenReturn(Optional.of(otherSubjectChapter));

        AskChatbotRequest request = new AskChatbotRequest(SUBJECT_ID, chapterId, "Câu hỏi", List.of());
        ChatbotAnswerResponse response = chatbotService.ask(USER_ID, request);

        assertThat(response.hasUnlockedContent()).isFalse();
        assertThat(response.answer()).contains("không tồn tại");
        verify(aiService, never()).chat(any());
    }

    @Test
    void askWhenNoUnlockedChaptersInSubjectShouldPromptUser() {
        when(contentRepository.findAllChapter(SUBJECT_ID)).thenReturn(List.of());
        when(unlockedChapterRepository.findByUserIdAndChapterIdIn(eq(USER_ID), any())).thenReturn(List.of());

        AskChatbotRequest request = new AskChatbotRequest(SUBJECT_ID, null, "Câu hỏi môn học", List.of());
        ChatbotAnswerResponse response = chatbotService.ask(USER_ID, request);

        assertThat(response.hasUnlockedContent()).isFalse();
        assertThat(response.answer()).contains("Bạn chưa mở khóa nội dung lý thuyết nào");
        verify(aiService, never()).chat(any());
    }
}

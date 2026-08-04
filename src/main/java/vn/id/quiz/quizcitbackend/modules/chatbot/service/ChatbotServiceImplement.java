package vn.id.quiz.quizcitbackend.modules.chatbot.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.ai.dto.ChatRequest;
import vn.id.quiz.quizcitbackend.modules.ai.service.AIService;
import vn.id.quiz.quizcitbackend.modules.chatbot.presentation.dto.AskChatbotRequest;
import vn.id.quiz.quizcitbackend.modules.chatbot.presentation.dto.ChatSourceResponse;
import vn.id.quiz.quizcitbackend.modules.chatbot.presentation.dto.ChatbotAnswerResponse;
import vn.id.quiz.quizcitbackend.modules.chatbot.repository.TheoryChunkMatch;
import vn.id.quiz.quizcitbackend.modules.chatbot.repository.TheoryChunkRepository;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaUserUnlockedChapterRepository;
import vn.id.quiz.quizcitbackend.modules.content.service.SubjectVisibilityGuard;

/**
 * Chatbox trả lời câu hỏi lý thuyết bằng RAG (pgvector), với 2 lớp ràng buộc
 * quan trọng:
 * <ol>
 *   <li><b>Không lộ đáp án trắc nghiệm:</b> {@code context} truy hồi CHỈ từ
 *       {@code theory_chunks} (index từ {@link Chapter}/Theory), không bao
 *       giờ động tới {@code Question}/{@code Option} — AI vật lý không có
 *       đường nào chạm vào đáp án. Lớp bảo vệ thứ 2 là system prompt ở AI
 *       Service (xem {@code rag_chat.py}).</li>
 *   <li><b>Giới hạn theo tiến độ mở khóa:</b> phạm vi tìm kiếm luôn được lọc
 *       theo {@code unlockedChapterIds} của user trong subject — chương nào
 *       chưa mở khóa thì chunk của chương đó không bao giờ lọt vào context,
 *       bất kể câu hỏi có nhắc tới hay không.</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class ChatbotServiceImplement implements ChatbotService {

    private static final int TOP_K = 5;
    private static final int SNIPPET_LENGTH = 160;

    private final ContentRepository contentRepository;
    private final JpaUserUnlockedChapterRepository unlockedChapterRepository;
    private final SubjectVisibilityGuard visibilityGuard;
    private final TheoryChunkRepository theoryChunkRepository;
    private final AIService aiService;

    @Override
    public ChatbotAnswerResponse ask(String userId, AskChatbotRequest request) {
        visibilityGuard.requireVisibleSubject(request.subjectId());

        Set<String> unlockedChapterIds = unlockedChaptersOf(userId, request.subjectId());
        if (unlockedChapterIds.isEmpty()) {
            return new ChatbotAnswerResponse(
                    "Bạn chưa mở khóa nội dung lý thuyết nào trong môn học này, nên mình chưa "
                            + "thể hỗ trợ được. Hãy hoàn thành các bài đánh giá đầu vào để mở khóa "
                            + "chương đầu tiên nhé!",
                    List.of(),
                    false);
        }

        List<float[]> questionEmbedding = aiService.embedTexts(List.of(request.message()));
        List<TheoryChunkMatch> matches = questionEmbedding == null || questionEmbedding.isEmpty()
                ? List.of()
                : theoryChunkRepository.findTopK(questionEmbedding.get(0), unlockedChapterIds, TOP_K);

        List<String> contextTexts = matches.stream().map(TheoryChunkMatch::content).toList();

        String answer = aiService.chat(new ChatRequest(
                request.message(),
                contextTexts,
                request.history(),
                contextTexts.isEmpty()));

        List<ChatSourceResponse> sources = matches.stream()
                .map(match -> new ChatSourceResponse(
                        match.theoryId(),
                        match.chapterId(),
                        snippet(match.content())))
                .toList();

        return new ChatbotAnswerResponse(answer, sources, true);
    }

    private Set<String> unlockedChaptersOf(String userId, String subjectId) {
        List<Chapter> chapters = contentRepository.findAllChapter(subjectId);
        List<String> chapterIds = chapters.stream().map(Chapter::getChapterId).toList();
        return unlockedChapterRepository.findByUserIdAndChapterIdIn(userId, chapterIds).stream()
                .map(unlocked -> unlocked.getChapterId())
                .collect(Collectors.toSet());
    }

    private String snippet(String content) {
        if (content == null) {
            return "";
        }
        String trimmed = content.trim();
        return trimmed.length() <= SNIPPET_LENGTH ? trimmed : trimmed.substring(0, SNIPPET_LENGTH) + "…";
    }
}

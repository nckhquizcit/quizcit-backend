package vn.id.quiz.quizcitbackend.modules.chatbot.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.id.quiz.quizcitbackend.modules.ai.service.AIService;
import vn.id.quiz.quizcitbackend.modules.chatbot.repository.TheoryChunkRepository;
import vn.id.quiz.quizcitbackend.modules.chatbot.util.BlockNoteTextExtractor;
import vn.id.quiz.quizcitbackend.modules.chatbot.util.TextChunker;

/**
 * (Re)index nội dung của 1 {@code Theory} thành các {@code theory_chunks} có
 * embedding, phục vụ chatbot RAG. Được gọi từ
 * {@code ContentServiceImplement} mỗi khi lý thuyết được tạo/sửa/xóa.
 *
 * <p>Lỗi index (AI Service không phản hồi embedding) KHÔNG được để văng lên
 * làm hỏng thao tác CRUD lý thuyết của admin — chỉ log cảnh báo. Admin có
 * thể gọi lại {@link #reindex} thủ công sau (xem endpoint admin reindex).</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TheoryIndexingService {

    private final AIService aiService;
    private final TheoryChunkRepository theoryChunkRepository;

    public int index(String theoryId, String chapterId, String content, String contentFormat) {
        theoryChunkRepository.deleteByTheoryId(theoryId);

        String plainText = BlockNoteTextExtractor.toPlainText(content, contentFormat);
        List<String> chunks = TextChunker.chunk(plainText);
        if (chunks.isEmpty()) {
            return 0;
        }

        List<float[]> embeddings = aiService.embedTexts(chunks);
        if (embeddings == null || embeddings.size() != chunks.size()) {
            log.warn("Bỏ qua index theory {} vì AI Service không trả embedding hợp lệ", theoryId);
            return 0;
        }

        theoryChunkRepository.insertChunks(theoryId, chapterId, chunks, embeddings);
        return chunks.size();
    }

    public void delete(String theoryId) {
        theoryChunkRepository.deleteByTheoryId(theoryId);
    }

    /** Dùng lại cho endpoint admin reindex thủ công. */
    public int reindex(String theoryId, String chapterId, String content, String contentFormat) {
        return index(theoryId, chapterId, content, contentFormat);
    }
}

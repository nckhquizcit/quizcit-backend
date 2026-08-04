package vn.id.quiz.quizcitbackend.modules.chatbot.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Chia plain text thành các chunk vừa đủ dùng cho embedding + RAG. Chunk
 * theo đoạn (paragraph) trước, gộp các đoạn ngắn liên tiếp lại cho tới khi
 * gần chạm {@code targetSize}, tránh chunk quá nhỏ (thiếu ngữ cảnh) hoặc quá
 * lớn (loãng embedding / tốn token).
 */
public final class TextChunker {

    private static final int TARGET_SIZE = 800;
    private static final int MIN_CHUNK_SIZE = 60;

    private TextChunker() {
    }

    public static List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }

        String[] paragraphs = text.split("\\n{1,}");
        StringBuilder current = new StringBuilder();

        for (String rawParagraph : paragraphs) {
            String paragraph = rawParagraph.trim();
            if (paragraph.isEmpty()) {
                continue;
            }

            if (current.length() + paragraph.length() + 1 > TARGET_SIZE && current.length() >= MIN_CHUNK_SIZE) {
                chunks.add(current.toString().trim());
                current.setLength(0);
            }

            current.append(paragraph).append('\n');

            // Đoạn tự nó đã quá dài -> chốt luôn thành 1 chunk riêng.
            if (current.length() > TARGET_SIZE) {
                chunks.add(current.toString().trim());
                current.setLength(0);
            }
        }

        if (!current.isEmpty()) {
            chunks.add(current.toString().trim());
        }

        return chunks;
    }
}

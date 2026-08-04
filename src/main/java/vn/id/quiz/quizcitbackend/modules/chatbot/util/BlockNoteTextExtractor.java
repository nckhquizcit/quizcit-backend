package vn.id.quiz.quizcitbackend.modules.chatbot.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Trích xuất plain text từ nội dung {@code Theory.content}, vốn có thể là
 * plain text thường hoặc JSON block của BlockNote editor (xem
 * {@code contentFormat} — "plain" | "blocknote").
 *
 * <p>Cần bước này trước khi chunk + embed nội dung lý thuyết cho RAG, vì
 * embedding JSON thô (chứa id, type, props...) sẽ làm loãng ngữ nghĩa và tốn
 * token vô ích. Tương đương về mặt chức năng với
 * {@code richTextBlocks.js -> blocksToPlainText} ở phía frontend, nhưng viết
 * lại bằng Jackson cho phía Java.</p>
 */
public final class BlockNoteTextExtractor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private BlockNoteTextExtractor() {
    }

    public static String toPlainText(String content, String contentFormat) {
        if (content == null || content.isBlank()) {
            return "";
        }
        if (!"blocknote".equalsIgnoreCase(contentFormat)) {
            return content.trim();
        }

        try {
            JsonNode root = MAPPER.readTree(content);
            StringBuilder builder = new StringBuilder();
            appendNode(root, builder);
            return builder.toString().replaceAll("[ \\t]+", " ").trim();
        } catch (Exception ex) {
            // Nội dung không parse được như JSON hợp lệ -> coi như plain text,
            // tránh làm hỏng luồng index chỉ vì 1 theory bị lỗi định dạng.
            return content.trim();
        }
    }

    private static void appendNode(JsonNode node, StringBuilder builder) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                appendNode(child, builder);
            }
            return;
        }
        if (!node.isObject()) {
            return;
        }

        // Text run: { "type": "text", "text": "..." }
        JsonNode textNode = node.get("text");
        if (textNode != null && textNode.isTextual()) {
            builder.append(textNode.asText()).append(' ');
        }

        JsonNode content = node.get("content");
        if (content != null) {
            appendNode(content, builder);
        }

        JsonNode children = node.get("children");
        if (children != null) {
            appendNode(children, builder);
        }

        // Mỗi block (paragraph/heading/listItem/...) xuống dòng để chunk theo
        // đoạn tự nhiên hơn, thay vì dồn hết thành 1 dòng dài.
        if (node.has("type") && (content != null || children != null)) {
            builder.append('\n');
        }
    }
}

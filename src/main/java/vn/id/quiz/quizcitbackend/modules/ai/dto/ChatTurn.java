package vn.id.quiz.quizcitbackend.modules.ai.dto;

/**
 * Một lượt hội thoại trong lịch sử chat gửi kèm cho AI Service để giữ ngữ
 * cảnh (không cần AI Service tự lưu trạng thái).
 *
 * @param role "user" hoặc "assistant"
 * @param text nội dung lượt chat
 */
public record ChatTurn(String role, String text) {

    public static ChatTurn user(String text) {
        return new ChatTurn("user", text);
    }

    public static ChatTurn assistant(String text) {
        return new ChatTurn("assistant", text);
    }
}

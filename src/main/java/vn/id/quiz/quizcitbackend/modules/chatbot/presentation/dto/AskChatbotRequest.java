package vn.id.quiz.quizcitbackend.modules.chatbot.presentation.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import vn.id.quiz.quizcitbackend.modules.ai.dto.ChatTurn;

/**
 * @param subjectId môn học sinh viên đang xem — dùng để xác định tập chương
 *                  đã mở khóa (phạm vi kiến thức được phép trả lời)
 * @param chapterId chương hiện tại (optional) — chỉ để ưu tiên ngữ cảnh, KHÔNG
 *                  giới hạn phạm vi (SV có thể hỏi lại nội dung chương trước)
 * @param message   câu hỏi của sinh viên
 * @param history   lịch sử hội thoại gần đây, FE tự giữ state và gửi kèm
 */
public record AskChatbotRequest(
        @NotBlank String subjectId,
        String chapterId,
        @NotBlank String message,
        List<ChatTurn> history) {

    public AskChatbotRequest {
        history = history == null ? List.of() : history;
    }
}

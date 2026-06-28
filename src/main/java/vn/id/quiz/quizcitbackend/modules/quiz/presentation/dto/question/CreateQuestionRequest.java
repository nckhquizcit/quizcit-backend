package vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.question;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.option.CreateOptionRequest;

/**
 * Request body cho {@code POST /api/questions}.
 *
 * <p><b>Fix bug:</b> Trước đây field là {@code explaination}. Đổi sang
 * {@code explanation} cho đúng schema.</p>
 */
@Getter
@Setter
public class CreateQuestionRequest {

    private String subjectId;

    private String chapterId;

    private String content;

    private String explanation;

    private double difficultyB;

    private List<CreateOptionRequest> options;

    /**
     * Danh sách ID kỹ năng (skill) gắn với câu hỏi.
     * Hỗ trợ Phase 2 (P1): API quản lý kỹ năng / IRT.
     */
    private List<QuestionSkillMappingRequest> skills;
}

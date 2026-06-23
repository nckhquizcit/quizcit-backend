package vn.id.quiz.quizcitbackend.modules.quiz.presentation.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Entity cho bảng {@code questions}.
 *
 * <p><b>Fix bug:</b> Trước đây column được map là {@code explaination} (sai
 * chính tả). Database thực tế chỉ có cột {@code explanation}. Khi JPA cố gắng
 * truy vấn sẽ ném lỗi "column explaination does not exist" và API tạo/sửa
 * câu hỏi hoặc tạo quiz trả về 500. Tên cột đã được chuẩn hoá về
 * {@code explanation} và field Java cũng đổi sang {@code explanation} để
 * code rõ ràng. Một file migration SQL
 * ({@code V3__rename_questions_explaination.sql}) được cung cấp để
 * rename cột trong database nếu schema cũ vẫn dùng {@code explaination}.</p>
 */
@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Question {
    @Id
    @Column(name = "question_id", length = 50)
    private String questionId;

    @Column(name = "subject_id", nullable = false, length = 50)
    private String subjectId;

    @Column(name = "chapter_id", nullable = false, length = 50)
    private String chapterId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "difficulty_b")
    private double difficultyB;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "is_active")
    private boolean isActive;

    public static Question create(
            String subjectId,
            String chapterId,
            String content,
            String explanation,
            double difficultyB) {

        return Question.builder()
                .questionId(UUID.randomUUID().toString())
                .subjectId(subjectId)
                .chapterId(chapterId)
                .content(content)
                .explanation(explanation)
                .difficultyB(difficultyB)
                .createdAt(ZonedDateTime.now())
                .isActive(true)
                .build();
    }
}

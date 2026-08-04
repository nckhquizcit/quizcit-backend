package vn.id.quiz.quizcitbackend.modules.quiz.presentation.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;

@Entity
@Table(name = "attempts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Attempt {

    @Id
    @Column(name = "attempt_id", length = 50)
    private String attemptId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "quiz_id", nullable = false, length = 50)
    private String quizId;

    @Column(name = "raw_score")
    private double rawScore;

    @Column(name = "total_question")
    private int totalQuestion;
    @Column(name = "overall_theta")
    private double overallTheta;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    // MỚI: lời khuyên cải thiện được AI sinh 1 LẦN tại thời điểm nộp bài
    // (dựa trên delta theta từng skill), lưu lại để xem kết quả không phải
    // gọi lại LLM mỗi lần tải trang.
    @Column(name = "advice_text", columnDefinition = "TEXT")
    private String adviceText;

    public static Attempt create(
            String userId,
            String quizId,
            int totalQuestion) {

        return Attempt.builder()
                .attemptId(UUID.randomUUID().toString())
                .userId(userId)
                .quizId(quizId)
                .totalQuestion(totalQuestion)
                .createdAt(ZonedDateTime.now())
                .build();
    }

    public void complete(
            double rawScore,
            double overallTheta) {

        this.rawScore = rawScore;
        this.overallTheta = overallTheta;
    }

    /** MỚI: gắn lời khuyên cải thiện sau khi AI Service sinh xong. */
    public void attachAdvice(String adviceText) {
        this.adviceText = adviceText;
    }
}

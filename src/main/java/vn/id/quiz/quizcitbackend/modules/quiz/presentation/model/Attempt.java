package vn.id.quiz.quizcitbackend.modules.quiz.presentation.model;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Column(name = "deadline_at")
    private ZonedDateTime deadlineAt;

    @Column(name = "submitted_at")
    private ZonedDateTime submittedAt;

    @ElementCollection
    @CollectionTable(name = "attempt_draft_answers", joinColumns = @JoinColumn(name = "attempt_id"))
    @MapKeyColumn(name = "question_id", length = 50)
    @Column(name = "option_id", nullable = false, length = 50)
    @Builder.Default
    private Map<String, String> draftAnswers = new HashMap<>();

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

    public static Attempt create(
            String userId,
            String quizId,
            int totalQuestion,
            int durationMinutes) {
        ZonedDateTime now = ZonedDateTime.now();
        return Attempt.builder()
                .attemptId(UUID.randomUUID().toString())
                .userId(userId)
                .quizId(quizId)
                .totalQuestion(totalQuestion)
                .createdAt(now)
                .deadlineAt(durationMinutes > 0 ? now.plusMinutes(durationMinutes) : null)
                .status(AttemptStatus.IN_PROGRESS)
                .build();
    }

    public void complete(
            double rawScore,
            double overallTheta) {

        this.rawScore = rawScore;
        this.overallTheta = overallTheta;
        this.submittedAt = ZonedDateTime.now();
        this.status = AttemptStatus.SUBMITTED;
    }

    public void markTimedOut() {
        this.status = AttemptStatus.TIMED_OUT;
    }

    public void saveDraftAnswers(Map<String, String> answers) {
        this.draftAnswers.clear();
        this.draftAnswers.putAll(answers);
    }

    /** MỚI: gắn lời khuyên cải thiện sau khi AI Service sinh xong. */
    public void attachAdvice(String adviceText) {
        this.adviceText = adviceText;
    }
}

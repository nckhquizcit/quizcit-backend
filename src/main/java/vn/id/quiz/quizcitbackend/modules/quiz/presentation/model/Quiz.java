package vn.id.quiz.quizcitbackend.modules.quiz.presentation.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "quizzes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
public class Quiz {

    @Id
    @Column(name = "quiz_id", length = 50)
    private String quizId;

    @Column(name = "subject_id", nullable = false, length = 50)
    private String subjectId;

    @Column(name = "chapter_id", nullable = false, length = 50)
    private String chapterId;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private QuizType type;

    @Column(name = "total_question")
    private int totalQuestion;

    private int duration;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;

    public static Quiz create(
            String subjectId,
            String chapterId,
            String name,
            int totalQuestion,
            QuizType type,
            int duration,
            int displayOrder) {

        return Quiz.builder()
                .quizId(UUID.randomUUID().toString())
                .subjectId(subjectId)
                .chapterId(chapterId)
                .name(name)
                .totalQuestion(totalQuestion)
                .type(type)
                .duration(duration)
                .displayOrder(displayOrder)
                .createdAt(ZonedDateTime.now())
                .build();
    }

    public void updateDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void softDelete() {
        this.deletedAt = ZonedDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}

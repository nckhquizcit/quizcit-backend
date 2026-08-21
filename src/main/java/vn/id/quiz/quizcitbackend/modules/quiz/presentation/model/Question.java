package vn.id.quiz.quizcitbackend.modules.quiz.presentation.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
public class Question {
    @Id
    @Column(name = "question_id", length = 50)
    private String questionId;

    @Column(name = "subject_id", length = 50)
    private String subjectId;

    @Column(name = "chapter_id", length = 50)
    private String chapterId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "content_format", length = 20)
    @Builder.Default
    private String contentFormat = "plain";

    @Column(name = "difficulty_b")
    private double difficultyB;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;

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
                .build();
    }

    public void softDelete() {
        this.deletedAt = ZonedDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}

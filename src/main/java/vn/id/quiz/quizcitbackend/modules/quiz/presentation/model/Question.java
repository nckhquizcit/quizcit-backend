package vn.id.quiz.quizcitbackend.modules.quiz.presentation.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;


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
}

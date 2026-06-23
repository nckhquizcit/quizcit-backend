package vn.id.quiz.quizcitbackend.modules.quiz.presentation.model;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quizzes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
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

    public static Quiz create(
            String subjectId,
            String chapterId,
            String name,
            int totalQuestion,
            QuizType type,
            int duration) {

        return Quiz.builder()
                .quizId(UUID.randomUUID().toString())
                .subjectId(subjectId)
                .chapterId(chapterId)
                .name(name)
                .totalQuestion(totalQuestion)
                .type(type)
                .duration(duration)
                .build();
    }
}
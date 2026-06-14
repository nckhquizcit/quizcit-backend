package vn.id.quiz.quizcitbackend.modules.quiz.presentation.model;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;

@Entity
@Table(name = "quizzes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Quiz {

    @Id
    @Column(name = "quiz_id")
    private String quizId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject; //Đổi lại rồi, dùng ID chứ không cần nhét nguyên cái enity vô nữa

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private Chapter chapter; //Đổi lại rồi, dùng ID chứ không cần nhét nguyên cái enity vô nữa

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private QuizType type;

    @Column(name = "total_question")
    private int totalQuestion;

    private int duration;

    public static Quiz create(
            Subject subject,
            Chapter chapter,
            String name,
            int totalQuestion,
            QuizType type,
            int duration
    ) {
        return Quiz.builder()
                .quizId(UUID.randomUUID().toString())
                .subject(subject)
                .chapter(chapter)
                .name(name)
                .totalQuestion(totalQuestion)
                .type(type)
                .duration(duration)
                .build();
    }
}
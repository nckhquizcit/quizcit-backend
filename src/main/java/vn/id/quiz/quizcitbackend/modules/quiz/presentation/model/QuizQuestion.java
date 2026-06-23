package vn.id.quiz.quizcitbackend.modules.quiz.presentation.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quiz_questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class QuizQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 50)
    private String id;

    @Column(name = "quiz_id", nullable = false, length = 50)
    private String quizId;

    @Column(name = "question_id", nullable = false, length = 50)
    private String questionId;

    @Column(name = "index_order")
    private int indexOrder;

    public static QuizQuestion create(
            String quizId,
            String questionId,
            int indexOrder) {

        return QuizQuestion.builder()
                .quizId(quizId)
                .questionId(questionId)
                .indexOrder(indexOrder)
                .build();
    }
}

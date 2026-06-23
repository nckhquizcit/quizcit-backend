package vn.id.quiz.quizcitbackend.modules.quiz.presentation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quiz_questions")
@IdClass(QuizQuestionId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class QuizQuestion {
    @Id
    @Column(name = "quiz_id", nullable = false, length = 50)
    private String quizId;

    @Id
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

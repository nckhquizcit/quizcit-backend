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
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz; //Đổi lại rồi, dùng ID chứ không cần nhét nguyên cái enity vô nữa

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question; //Đổi lại rồi, dùng ID chứ không cần nhét nguyên cái enity vô nữa

    @Column(name = "index_order")
    private int indexOrder;
}

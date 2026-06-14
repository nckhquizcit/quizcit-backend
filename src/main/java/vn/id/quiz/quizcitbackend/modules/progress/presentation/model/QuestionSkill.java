package vn.id.quiz.quizcitbackend.modules.progress.presentation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "question_skills")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class QuestionSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "question_id", nullable = false, length = 50)
    private String questionId;

    @Column(name = "skill_id", nullable = false, length = 50)
    private String skillId;

    @Column(nullable = false)
    private double weight;

    @Column(name = "is_primary")
    private boolean isPrimary;

    public static QuestionSkill create(
            String questionId,
            String skillId,
            double weight,
            boolean isPrimary) {
        return QuestionSkill.builder()
                .questionId(questionId)
                .skillId(skillId)
                .weight(weight)
                .isPrimary(isPrimary)
                .build();
    }
}
package vn.id.quiz.quizcitbackend.modules.quiz.presentation.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "responses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Response {

    @Id
    @Column(name = "response_id", length = 50)
    private String responseId;

    @Column(name = "attempt_id", nullable = false, length = 50)
    private String attemptId;

    @Column(name = "question_id", nullable = false, length = 50)
    private String questionId;

    @Column(name = "option_id", length = 50)
    private String optionId;

    @Column(name = "score")
    private boolean score;

    public static Response create(
            String attemptId,
            String questionId,
            String optionId,
            boolean score) {
        return Response.builder()
                .responseId(UUID.randomUUID().toString())
                .attemptId(attemptId)
                .questionId(questionId)
                .optionId(optionId)
                .score(score)
                .build();
    }
}
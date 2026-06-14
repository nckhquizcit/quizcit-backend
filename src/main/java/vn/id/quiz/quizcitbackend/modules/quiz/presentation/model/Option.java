package vn.id.quiz.quizcitbackend.modules.quiz.presentation.model;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Option {
    @Id
    @Column(name = "option_id", length = 50)
    private String optionId;

    @Column(name = "question_id", nullable = false)
    private String questionId;

    @Column(name = "content", columnDefinition = "TEXT") // Thêm text để không bị tràng content của cột
    private String content;

    @Column(name = "is_corrected")
    private boolean isCorrected;

    public static Option create(
            String questionId,
            String content,
            boolean isCorrected) {
        return Option.builder()
                .optionId(UUID.randomUUID().toString())
                .questionId(questionId)
                .content(content)
                .isCorrected(isCorrected)
                .build();
    }
}

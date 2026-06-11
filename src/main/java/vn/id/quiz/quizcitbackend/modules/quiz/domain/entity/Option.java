package vn.id.quiz.quizcitbackend.modules.quiz.domain.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Option {
    @Id
    @Column(name="option_id", length = 50)
    private String optionId;
// bỏ tham chiếu đến question vì không bao giờ cần truy ngược option này của question nào
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name="question_id", nullable = false)
//    private Question question;

    @Column(name="content", columnDefinition = "TEXT") // Thêm text để không bị tràng content của cột
    private String content;

    @Column(name="is_corrected")
    private boolean isCorrected;

}

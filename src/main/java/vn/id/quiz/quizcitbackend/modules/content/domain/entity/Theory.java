package vn.id.quiz.quizcitbackend.modules.content.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "theories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Theory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "theory_id", length = 50)
    private String theoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
    }
}
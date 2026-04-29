package vn.id.quiz.quizcitbackend.modules.content.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "chapters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "chapter_id", length = 50)
    private String chapterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL)
    private List<Theory> theories;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
    }
}
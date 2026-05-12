package vn.id.quiz.quizcitbackend.modules.content.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "theories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Theory {
    @Id
    @Column(name = "theory_id", length = 50)
    private String theoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    public static Theory create(Chapter chapter, String content) {
        return Theory.builder()
                .theoryId(UUID.randomUUID().toString())
                .chapter(chapter)
                .content(content)
                .createdAt(ZonedDateTime.now())
                .build();
    }
    public void updateTheory(String content){
        this.content = content;
    }

}
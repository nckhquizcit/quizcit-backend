package vn.id.quiz.quizcitbackend.modules.content.presentation.model;

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

    @Column(name = "chapter_id", nullable = false)
    private String chapterId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    public static Theory create(String chapterId, String content) {
        return Theory.builder()
                .theoryId(UUID.randomUUID().toString())
                .chapterId(chapterId)
                .content(content)
                .createdAt(ZonedDateTime.now())
                .build();
    }
    public void updateTheory(String content){
        this.content = content;
    }
}
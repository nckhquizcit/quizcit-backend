package vn.id.quiz.quizcitbackend.modules.content.presentation.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "theories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
public class Theory {
    @Id
    @Column(name = "theory_id", length = 50)
    private String theoryId;

    @Column(name = "chapter_id", nullable = false)
    private String chapterId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "content_format", length = 20)
    @Builder.Default
    private String contentFormat = "plain";

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;

    public static Theory create(String chapterId, String content) {
        return create(chapterId, content, "plain");
    }

    public static Theory create(String chapterId, String content, String contentFormat) {
        return Theory.builder()
                .theoryId(UUID.randomUUID().toString())
                .chapterId(chapterId)
                .content(content)
                .contentFormat(normalizeContentFormat(contentFormat))
                .createdAt(ZonedDateTime.now())
                .build();
    }

    public void updateTheory(String content, String contentFormat){
        this.content = content;
        this.contentFormat = normalizeContentFormat(contentFormat);
    }

    public void updateTheory(String content){
        updateTheory(content, "plain");
    }

    public String getContentFormat() {
        return normalizeContentFormat(contentFormat);
    }

    public void softDelete() {
        this.deletedAt = ZonedDateTime.now();
    }

    private static String normalizeContentFormat(String value) {
        return "blocknote".equalsIgnoreCase(value) ? "blocknote" : "plain";
    }
}

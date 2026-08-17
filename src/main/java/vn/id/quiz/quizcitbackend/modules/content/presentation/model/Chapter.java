package vn.id.quiz.quizcitbackend.modules.content.presentation.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "chapters")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
public class Chapter {
    @Id
    @Column(name = "chapter_id", length = 50)
    private String chapterId;

    @Column(name = "subject_id", nullable = false, length = 50)
    private String subjectId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;

    public static Chapter create(String subjectId, String name, int displayOrder){
        return Chapter.builder()
                .chapterId(UUID.randomUUID().toString())
                .subjectId(subjectId)
                .name(name)
                .createdAt(ZonedDateTime.now())
                .displayOrder(displayOrder)
                .build();
    }
    public void updateChapter(String name, int displayOrder){
        this.name=name;
        this.displayOrder=displayOrder;
    }
    public void updateDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void softDelete() {
        this.deletedAt = ZonedDateTime.now();
    }
}

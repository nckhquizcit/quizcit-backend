package vn.id.quiz.quizcitbackend.modules.content.presentation.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "chapters")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
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

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private final List<Theory> theories = new ArrayList<>();

    public static Chapter create(String subjectId, String name){
        return Chapter.builder()
                .chapterId(UUID.randomUUID().toString())
                .subjectId(subjectId)
                .name(name)
                .createdAt(ZonedDateTime.now())
                .build();
    }
    public void updateChapter(String name){
        this.name=name;
    }
}
package vn.id.quiz.quizcitbackend.modules.content.domain.entity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private final List<Theory> theories = new ArrayList<>();

    public static Chapter create(Subject subject, String name) {
        return Chapter.builder()
                .chapterId(UUID.randomUUID().toString())
                .subject(subject)
                .name(name)
                .createdAt(ZonedDateTime.now())
                .build();
    }

    public Theory addTheory(String content){
        if(content==null||content.isBlank()){
            throw new IllegalArgumentException("Nội dung lý thuyết không được để trống");
        }
        Theory newTheory = Theory.create(this,content);
        this.theories.add(newTheory);
        return newTheory;
    }
}
package vn.id.quiz.quizcitbackend.modules.content.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.http.HttpStatus;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

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
    public List<Theory> getAllTheories(){
        return this.theories;
    }
    private Theory findTheoryById(String theoryId) {
        return this.theories.stream()
                .filter(t -> t.getTheoryId().equals(theoryId))
                .findFirst()
                .orElseThrow(() -> new AppException("Không tìm thấy nội dung lý thuyết được yêu cầu.", HttpStatus.NOT_FOUND));
    }
    /**
     * Không đổi subject vì làm gì có vụ đổi chương của môn A sang môn B đc :D
     * @param name
     */
    public void updateChapter(String name){
        this.name = name;
    }

    public void updateTheory(String theoryId, String content){
        Theory theory = findTheoryById(theoryId);
        theory.updateTheory(content);
    }
    public void deleteTheory(String theoryId){
        boolean removed = this.theories.removeIf(theory -> theory.getTheoryId().equals(theoryId));
        if(!removed){
            throw new AppException("Không tìm thấy nội dung lý thuyết được yêu cầu", HttpStatus.NOT_FOUND);
        }
    }
}
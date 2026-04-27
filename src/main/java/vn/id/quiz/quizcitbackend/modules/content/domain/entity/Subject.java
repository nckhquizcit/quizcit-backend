package vn.id.quiz.quizcitbackend.modules.content.domain.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "subjects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {
    @Id
    @Column(name = "subject_id", length = 50) // cơ bản mặc định length của UUID là 36 nhưng để vầy đề phòng đổi bên cung cấp UUID
    private String subjectId;

    @Column(name = "course_code", unique = true, nullable = false, length = 20)
    private String courseCode;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL) //map với field subject trong chapter
    private List<Chapter> chapters;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
    }
}
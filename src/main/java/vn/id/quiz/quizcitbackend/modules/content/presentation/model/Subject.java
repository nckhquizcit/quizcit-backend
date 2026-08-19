package vn.id.quiz.quizcitbackend.modules.content.presentation.model;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "subjects")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
public class Subject {
    @Id
    @Column(name = "subject_id", length = 50) // cơ bản mặc định length của UUID là 36 nhưng để vầy đề phòng đổi bên cung cấp UUID
    private String subjectId;

    @Column(name = "course_code", nullable = false, length = 20)
    private String courseCode;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_visible", nullable = false)
    @Builder.Default
    private boolean visible = true;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;

    public static Subject create(String courseCode, String name, String description, int displayOrder, String imageUrl){
        if(courseCode==null||courseCode.isBlank()){
            throw new IllegalArgumentException("Mã học phần không được để trống");
        }
        if(name==null||name.isBlank()){
            throw new IllegalArgumentException("Tên học phần không được để trống");
        }
        return Subject.builder()
                .subjectId(UUID.randomUUID().toString()) // Chủ động sinh ID
                .courseCode(courseCode)
                .name(name)
                .description(description)
                .imageUrl(imageUrl)
                .visible(true)
                .createdAt(ZonedDateTime.now())
                .displayOrder(displayOrder)
                .build();
    }

    public static Subject create(String courseCode, String name, String description, int displayOrder){
        return create(courseCode, name, description, displayOrder, null);
    }

    public void updateSubject(String courseCode, String name, String description, boolean visible, int displayOrder, String imageUrl){
        this.courseCode = courseCode;
        this.name = name;
        this.description = description;
        this.visible = visible;
        this.displayOrder = displayOrder;
        this.imageUrl = imageUrl;
    }

    public void updateSubject(String courseCode, String name, String description, boolean visible, int displayOrder){
        updateSubject(courseCode, name, description, visible, displayOrder, this.imageUrl);
    }

    public void updateDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void softDelete() {
        this.deletedAt = ZonedDateTime.now();
        this.visible = false;
    }
}

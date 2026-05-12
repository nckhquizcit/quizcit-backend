package vn.id.quiz.quizcitbackend.modules.content.domain.entity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.http.HttpStatus;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "subjects")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true) //map với field subject trong chapter
    @Builder.Default
    private final List<Chapter> chapters = new ArrayList<>();

    public static Subject create(String courseCode, String name, String description){
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
                .createdAt(ZonedDateTime.now())
                .build();
    }
    public Chapter addChapter(String chapterName){
        if(chapterName == null || chapterName.isBlank()){
            throw new IllegalArgumentException("Tên chương không được để trống");
        }
        Chapter newChapter = Chapter.create(this, chapterName);
        this.chapters.add(newChapter);
        return newChapter;
    }
    public Theory addTheory(String chapterId, String content){
        Chapter targetChapter = this.chapters.stream()
                .filter(c -> c.getChapterId().equals(chapterId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Chương không thuộc học phần này"));
        return targetChapter.addTheory(content);
    }

    public List<Chapter> getAllChapters(){
        return this.chapters;
    }
    //Nên xài findAllBySubjectId ở repository thay thằng này vì thằng này nặng lắm
    public List<Theory> getAllTheories(){
        return this.chapters.stream()
                .flatMap(chapter -> chapter.getAllTheories().stream())
                .sorted(Comparator.comparing(Theory::getCreatedAt))
                .toList();
    }
    public List<Theory> getAllTheories(String chapterId){
        Chapter chapter = findChapterById(chapterId);
        return chapter.getAllTheories();
    }
    public void updateSubject(String courseCode, String name, String description){
        this.courseCode= courseCode;
        this.name = name;
        this.description =description;
    }
    private Chapter findChapterById(String chapterId) {
        return this.chapters.stream()
                .filter(c -> c.getChapterId().equals(chapterId))
                .findFirst()
                .orElseThrow(() -> new AppException("Không tìm thấy chương được yêu cầu", HttpStatus.NOT_FOUND));
    }
    public void updateChapter(String chapterId, String name) {
        Chapter chapter = findChapterById(chapterId);
        chapter.updateChapter(name);
    }
    public void updateTheory(String chapterId, String theoryId, String content) {
        Chapter chapter = findChapterById(chapterId);
        chapter.updateTheory(theoryId, content);
    }

    public void deleteChapter(String chapterId){
        boolean removed = this.chapters.removeIf(chapter -> chapter.getChapterId().equals(chapterId));

        if (!removed) {
            throw new AppException("Không tìm thấy chương được yêu cầu", HttpStatus.NOT_FOUND);
        }
    }
    public void deleteTheory(String chapterId, String theoryId){
        Chapter chapter = findChapterById(chapterId);
        chapter.deleteTheory(theoryId);
    }

}
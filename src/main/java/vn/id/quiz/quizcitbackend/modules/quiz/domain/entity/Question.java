package vn.id.quiz.quizcitbackend.modules.quiz.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Subject;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Question {
    @Id
    @Column(name="question_id", length = 50)
    private String questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Column(name="content", columnDefinition = "TEXT") // Thêm text để không bị tràng content của cột
    private String content;

    @Column(name="explaination", columnDefinition = "TEXT") // Thêm text để không bị tràng content của cột
    private String explaination; // Đổi thành explaination để tránh trùng keyword với db

    @Column(name="difficulty_b")
    private double difficultyB;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "is_active")
    private boolean isActive;

    public static Question create(Subject subject, Chapter chapter, String content, String explaination, double difficultyB){
        return Question.builder()
        .questionId(UUID.randomUUID().toString())
        .subject(subject)
        .chapter(chapter)
        .content(content)
        .explaination(explaination)
        .difficultyB(difficultyB)
        .createdAt(ZonedDateTime.now())
        .isActive(true)
        .build();
    }
}

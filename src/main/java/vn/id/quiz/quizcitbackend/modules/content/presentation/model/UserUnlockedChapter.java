package vn.id.quiz.quizcitbackend.modules.content.presentation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_unlocked_chapters")
@IdClass(UserUnlockedChapterId.class)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserUnlockedChapter {
    @Id @Column(name = "user_id", length = 50)
    private String userId;
    @Id @Column(name = "chapter_id", length = 50)
    private String chapterId;

    public static UserUnlockedChapter create(String userId, String chapterId) {
        return UserUnlockedChapter.builder().userId(userId).chapterId(chapterId).build();
    }
}

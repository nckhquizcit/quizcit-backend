package vn.id.quiz.quizcitbackend.modules.user.presentation.model;

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
import org.springframework.http.HttpStatus;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

import java.time.ZonedDateTime;

@Entity
@Table(name = "user_achievement")
@IdClass(UserAchievementId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserAchievement {
    // Bang user_achievement dung khoa chinh ghep user_id + achieve_id.
    @Id
    @Column(name = "user_id", length = 50)
    private String userId;

    @Id
    @Column(name = "achieve_id", length = 50)
    private String achieveId;

    @Column(name = "is_achieved")
    private Boolean achieved;

    @Column(length = 100)
    private String badge;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "condition_type", length = 50)
    private String conditionType;

    @Column(name = "condition_value")
    private Integer conditionValue;

    @Column(name = "achieved_at")
    private ZonedDateTime achievedAt;

    // Tao thanh tuu chua dat; khi user dat dieu kien se goi markAchieved().
    public static UserAchievement create(
            String userId,
            String achieveId,
            String badge,
            String description,
            String conditionType,
            Integer conditionValue) {
        validateId(userId, "Nguoi dung khong hop le");
        validateId(achieveId, "Thanh tuu khong hop le");

        return UserAchievement.builder()
                .userId(userId)
                .achieveId(achieveId)
                .achieved(false)
                .badge(badge)
                .description(description)
                .conditionType(conditionType)
                .conditionValue(conditionValue)
                .build();
    }

    // Danh dau user da dat thanh tuu va luu thoi diem dat.
    public void markAchieved() {
        this.achieved = true;
        this.achievedAt = ZonedDateTime.now();
    }

    // Reset trang thai thanh tuu neu can tinh lai dieu kien.
    public void markUnachieved() {
        this.achieved = false;
        this.achievedAt = null;
    }

    private static void validateId(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new AppException(message, HttpStatus.BAD_REQUEST);
        }
    }
}

package vn.id.quiz.quizcitbackend.modules.user.presentation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
public class User {
    @Id
    // Khoa chinh cua bang users, tu sinh bang UUID dang String.
    @Column(name = "user_id", length = 50)
    private String userId;

    @Setter
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Setter
    @Column(nullable = false, length = 255)
    private String password;

    @Setter
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Setter
    @Column(name = "display_name", length = 100)
    private String displayName;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(length = 20, columnDefinition = "user_status_enum")
    private UserStatus status;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "token_version", nullable = false)
    @Builder.Default
    private Long tokenVersion = 0L;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;


    // Factory chi tao entity theo schema users va gan gia tri mac dinh.
    // Validation va nghiep vu chinh nam o service.
    public static User create(String username, String password, String email, String displayName) {
        ZonedDateTime now = ZonedDateTime.now();

        return User.builder()
                .userId(UUID.randomUUID().toString())
                .username(username.trim())
                .password(password)
                .email(email.trim())
                .displayName(normalizeDisplayName(displayName, username))
                .status(UserStatus.UNVERIFIED)
                .createdAt(now)
                .tokenVersion(0L)
                .build();
    }

    private static String normalizeDisplayName(String displayName, String username) {
        if (displayName == null || displayName.isBlank()) {
            return username.trim();
        }
        return displayName.trim();
    }

    public void revokeSessions() {
        tokenVersion = (tokenVersion == null ? 0L : tokenVersion) + 1;
    }

    public void softDelete() {
        deletedAt = ZonedDateTime.now();
        revokeSessions();
        username = "deleted_" + userId;
        email = "deleted_" + userId + "@deleted.invalid";
        displayName = "Người dùng đã xóa";
        password = "!deleted!";
        status = UserStatus.BANNED;
    }
}

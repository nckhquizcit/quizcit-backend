package vn.id.quiz.quizcitbackend.modules.user.domain.entity;

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
import org.springframework.http.HttpStatus;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {
    @Id
    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    public static User create(String username, String email, String displayName) {
        validateUsername(username);
        validateEmail(email);

        return User.builder()
                .userId(UUID.randomUUID().toString())
                .username(username.trim())
                .email(email.trim())
                .displayName(normalizeDisplayName(displayName, username))
                .role(UserRole.USER)
                .status(UserStatus.UNVERIFIED)
                .createdAt(ZonedDateTime.now())
                .build();
    }

    public void updateProfile(String username, String email, String displayName) {
        validateUsername(username);
        validateEmail(email);

        this.username = username.trim();
        this.email = email.trim();
        this.displayName = normalizeDisplayName(displayName, username);
    }

    public void changeRole(UserRole role) {
        if (role == null) {
            throw new AppException("Vai trò người dùng không hợp lệ", HttpStatus.BAD_REQUEST);
        }
        this.role = role;
    }

    public void changeStatus(UserStatus status) {
        if (status == null) {
            throw new AppException("Trạng thái người dùng không hợp lệ", HttpStatus.BAD_REQUEST);
        }
        this.status = status;
    }

    public void ban() {
        this.status = UserStatus.BANNED;
    }

    public void verify() {
        if (this.status == UserStatus.BANNED) {
            throw new AppException("Không thể xác minh tài khoản đang bị khóa", HttpStatus.CONFLICT);
        }
        this.status = UserStatus.VERIFIED;
    }

    private static void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new AppException("Tên đăng nhập không được để trống", HttpStatus.BAD_REQUEST);
        }
    }

    private static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new AppException("Email không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (!email.contains("@")) {
            throw new AppException("Email không hợp lệ", HttpStatus.BAD_REQUEST);
        }
    }

    private static String normalizeDisplayName(String displayName, String username) {
        if (displayName == null || displayName.isBlank()) {
            return username.trim();
        }
        return displayName.trim();
    }
}

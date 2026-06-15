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
    // Khoa chinh cua bang users, tu sinh bang UUID dang String.
    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserStatus status;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    // Factory tao user moi theo schema users va gan trang thai mac dinh UNVERIFIED.
    public static User create(String username, String password, String email, String displayName) {
        validateUsername(username);
        validatePassword(password);
        validateEmail(email);
        ZonedDateTime now = ZonedDateTime.now();

        return User.builder()
                .userId(UUID.randomUUID().toString())
                .username(username.trim())
                .password(password)
                .email(email.trim())
                .displayName(normalizeDisplayName(displayName, username))
                .status(UserStatus.UNVERIFIED)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    // Cap nhat cac field profile va dong bo updated_at.
    public void updateProfile(String username, String email, String displayName) {
        validateUsername(username);
        validateEmail(email);

        this.username = username.trim();
        this.email = email.trim();
        this.displayName = normalizeDisplayName(displayName, username);
        touch();
    }

    // Doi trang thai tai khoan: UNVERIFIED, VERIFIED hoac BANNED.
    public void changeStatus(UserStatus status) {
        if (status == null) {
            throw new AppException("Trang thai nguoi dung khong hop le", HttpStatus.BAD_REQUEST);
        }
        this.status = status;
        touch();
    }

    // Cap nhat mat khau va dong bo updated_at.
    public void changePassword(String password) {
        validatePassword(password);
        this.password = password;
        touch();
    }

    public void ban() {
        this.status = UserStatus.BANNED;
        touch();
    }

    public void verify() {
        if (this.status == UserStatus.BANNED) {
            throw new AppException("Khong the xac minh tai khoan dang bi khoa", HttpStatus.CONFLICT);
        }
        this.status = UserStatus.VERIFIED;
        touch();
    }

    private static void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new AppException("Ten dang nhap khong duoc de trong", HttpStatus.BAD_REQUEST);
        }
    }

    private static void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new AppException("Mat khau khong duoc de trong", HttpStatus.BAD_REQUEST);
        }
    }

    private static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new AppException("Email khong duoc de trong", HttpStatus.BAD_REQUEST);
        }
        if (!email.contains("@")) {
            throw new AppException("Email khong hop le", HttpStatus.BAD_REQUEST);
        }
    }

    private static String normalizeDisplayName(String displayName, String username) {
        if (displayName == null || displayName.isBlank()) {
            return username.trim();
        }
        return displayName.trim();
    }

    // Moi thay doi nghiep vu tren users deu cap nhat updated_at.
    private void touch() {
        this.updatedAt = ZonedDateTime.now();
    }
}

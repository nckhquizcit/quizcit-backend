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

import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @Column(name = "role_id", length = 50)
    private String roleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole name;

    public static Role create(UserRole name) {
        if (name == null) {
            throw new AppException("Vai trò người dùng không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        return Role.builder()
                .roleId(UUID.randomUUID().toString())
                .name(name)
                .build();
    }
}

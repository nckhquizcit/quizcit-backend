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
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

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

    /**
     * Mapping chính xác tên cột trong DB. Trước đây annotation
     * {@code @Column} không khai báo {@code name} nên Hibernate tự derive
     * thành {@code name} (khớp) nhưng cũng tạo cơ chế ambiguous. Ta chỉ định
     * rõ để chắc chắn mapping với cột {@code roles.name}.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, length = 50, unique = true)
    private UserRole name;

    public static Role create(UserRole name) {
        if (name == null) {
            throw new AppException(ErrorMessages.INVALID_ROLE, HttpStatus.BAD_REQUEST);
        }

        return Role.builder()
                .roleId(UUID.randomUUID().toString())
                .name(name)
                .build();
    }
}

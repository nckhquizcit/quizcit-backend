package vn.id.quiz.quizcitbackend.modules.user.presentation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.ZonedDateTime;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "permissions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
public class Permission {
    @Id
    @Column(name = "permission_id", length = 50)
    private String permissionId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;

    public static Permission create(String name) {
        if (name == null || name.isBlank()) {
            throw new AppException(ErrorMessages.INVALID_PERMISSION, HttpStatus.BAD_REQUEST);
        }

        return Permission.builder()
                .permissionId(UUID.randomUUID().toString())
                .name(name.trim())
                .build();
    }

    public void softDelete() {
        this.deletedAt = ZonedDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}

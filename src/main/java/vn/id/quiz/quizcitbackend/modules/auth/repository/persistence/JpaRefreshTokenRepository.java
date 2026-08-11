package vn.id.quiz.quizcitbackend.modules.auth.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.model.RefreshToken;

import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface JpaRefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RefreshToken r where r.tokenHash = :hash")
    Optional<RefreshToken> findByTokenHashForUpdate(@Param("hash") String hash);
}

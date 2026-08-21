package vn.id.quiz.quizcitbackend.modules.auth.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.model.AuthChallenge;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.model.AuthChallengePurpose;

import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface JpaAuthChallengeRepository extends JpaRepository<AuthChallenge, String> {
    Optional<AuthChallenge> findFirstByUserIdAndPurposeOrderByCreatedAtDesc(
            String userId, AuthChallengePurpose purpose);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from AuthChallenge c where c.challengeId = :id")
    Optional<AuthChallenge> findByIdForUpdate(@Param("id") String id);

    void deleteByUserId(String userId);
}

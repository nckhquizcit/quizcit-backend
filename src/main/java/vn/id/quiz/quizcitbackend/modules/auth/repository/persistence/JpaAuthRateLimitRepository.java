package vn.id.quiz.quizcitbackend.modules.auth.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.model.AuthRateLimit;
import vn.id.quiz.quizcitbackend.modules.auth.presentation.model.AuthRateLimitId;

public interface JpaAuthRateLimitRepository extends JpaRepository<AuthRateLimit, AuthRateLimitId> {
}

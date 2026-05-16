package vn.id.quiz.quizcitbackend.modules.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.user.domain.entity.User;

public interface JpaUserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsernameAndUserIdNot(String username, String userId);
    boolean existsByEmailAndUserIdNot(String email, String userId);
}

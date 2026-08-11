package vn.id.quiz.quizcitbackend.modules.user.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JpaUserRepository extends JpaRepository<User, String> {
    Page<User> findAll(Pageable pageable);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCaseAndUserIdNot(String username, String userId);
    boolean existsByEmailIgnoreCaseAndUserIdNot(String email, String userId);
}

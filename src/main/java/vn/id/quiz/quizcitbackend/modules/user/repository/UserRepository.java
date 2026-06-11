package vn.id.quiz.quizcitbackend.modules.user.repository;

import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> findAll();
    User save(User user);
    Optional<User> findUserById(String userId);
    boolean existsById(String userId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsernameAndUserIdNot(String username, String userId);
    boolean existsByEmailAndUserIdNot(String email, String userId);
    void deleteById(String userId);
}

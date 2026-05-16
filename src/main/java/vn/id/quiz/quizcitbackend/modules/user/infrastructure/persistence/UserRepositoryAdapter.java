package vn.id.quiz.quizcitbackend.modules.user.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.id.quiz.quizcitbackend.modules.user.domain.entity.User;
import vn.id.quiz.quizcitbackend.modules.user.domain.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {
    private final JpaUserRepository jpaUserRepository;

    @Override
    public List<User> findAll() {
        return jpaUserRepository.findAll();
    }

    @Override
    public User save(User user) {
        return jpaUserRepository.save(user);
    }

    @Override
    public Optional<User> findUserById(String userId) {
        return jpaUserRepository.findById(userId);
    }

    @Override
    public boolean existsById(String userId) {
        return jpaUserRepository.existsById(userId);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaUserRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsernameAndUserIdNot(String username, String userId) {
        return jpaUserRepository.existsByUsernameAndUserIdNot(username, userId);
    }

    @Override
    public boolean existsByEmailAndUserIdNot(String email, String userId) {
        return jpaUserRepository.existsByEmailAndUserIdNot(email, userId);
    }

    @Override
    public void deleteById(String userId) {
        jpaUserRepository.deleteById(userId);
    }
}

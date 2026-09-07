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

    @org.springframework.data.jpa.repository.Query(value = """
        SELECT u FROM User u
        WHERE (:search IS NULL OR :search = ''
               OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(u.displayName) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:status IS NULL OR u.status = :status)
          AND (:role IS NULL OR EXISTS (
              SELECT 1 FROM UserRoleAssignment ura
              WHERE ura.userId = u.userId AND ura.roleId IN (
                  SELECT r.roleId FROM Role r WHERE r.name = :role
              )
          ))
    """)
    Page<User> findUsers(
        @org.springframework.data.repository.query.Param("search") String search,
        @org.springframework.data.repository.query.Param("role") vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole role,
        @org.springframework.data.repository.query.Param("status") vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserStatus status,
        Pageable pageable
    );
}

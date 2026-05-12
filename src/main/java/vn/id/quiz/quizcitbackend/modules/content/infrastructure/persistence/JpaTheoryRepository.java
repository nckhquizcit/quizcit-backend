package vn.id.quiz.quizcitbackend.modules.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Theory;

public interface JpaTheoryRepository extends JpaRepository<Theory, String> {
}

package vn.id.quiz.quizcitbackend.modules.content.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Theory;
import vn.id.quiz.quizcitbackend.modules.content.domain.repository.TheoryRepository;
@Repository
public interface JpaTheoryRepository extends JpaRepository<Theory, String>, TheoryRepository {
}

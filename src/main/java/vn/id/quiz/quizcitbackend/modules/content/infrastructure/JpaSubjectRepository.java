package vn.id.quiz.quizcitbackend.modules.content.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Subject;
import vn.id.quiz.quizcitbackend.modules.content.domain.repository.SubjectRepository;

@Repository
public interface JpaSubjectRepository extends JpaRepository<Subject, String>, SubjectRepository { //interface được quyền đa thừa kế
    // Spring Data JPA sẽ tự động implement các method từ SubjectRepository
    // vì chúng trùng tên với các method mặc định của JpaRepository.
}

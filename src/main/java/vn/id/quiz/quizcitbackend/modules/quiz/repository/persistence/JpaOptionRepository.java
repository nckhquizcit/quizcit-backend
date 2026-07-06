package vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Option;

public interface JpaOptionRepository extends JpaRepository<Option, String> {

    List<Option> findByQuestionId(String questionId);

    // Batch: lấy option của nhiều câu hỏi trong 1 truy vấn (tránh N+1).
    List<Option> findByQuestionIdIn(Collection<String> questionIds);


    Optional<Option> findByOptionId(
            String optionId);

    void deleteByQuestionId(String questionId);
}

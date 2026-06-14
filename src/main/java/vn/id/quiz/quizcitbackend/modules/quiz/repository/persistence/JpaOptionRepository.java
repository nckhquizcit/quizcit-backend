package vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Option;

import java.util.List;

public interface JpaOptionRepository extends JpaRepository<Option, String> {

    List<Option> findByQuestionId(String questionId);

}
package vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, String> {

    List<Question> findByChapterChapterId(String chapterId);
};
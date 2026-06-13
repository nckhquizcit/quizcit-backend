package vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;

public interface JpaQuizRepository extends JpaRepository<Quiz, String>{
    //Hàm truy vấn cơ bản
}

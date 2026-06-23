package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.util.List;

import org.mapstruct.Mapper;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.QuizResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;

@Mapper(componentModel = "spring")
public interface QuizMapper {

    QuizResponse toResponse(Quiz quiz);

    List<QuizResponse> toResponseList(List<Quiz> quizzes);
}
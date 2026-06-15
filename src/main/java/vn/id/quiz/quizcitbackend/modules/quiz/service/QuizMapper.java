package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.QuizResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;

@Mapper(componentModel = "spring")
public interface QuizMapper {

    @Mapping(target = "subjectId", source = "subject.subjectId")
    @Mapping(target = "chapterId", source = "chapter.chapterId")
    QuizResponse toResponse(Quiz quiz);

    List<QuizResponse> toResponseList(List<Quiz> quizzes);
}
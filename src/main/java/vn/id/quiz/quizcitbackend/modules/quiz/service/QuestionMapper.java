package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.util.List;

import org.mapstruct.Mapper;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.QuestionResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    QuestionResponse toResponse(Question question);

    List<QuestionResponse> toResponseList(List<Question> questions);
}
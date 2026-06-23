package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.OptionResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.QuestionResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Option;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    @Mapping(target = "options", ignore = true)
    @Mapping(target = "correctOptionId", ignore = true)
    @Mapping(target = "skills", ignore = true)
    QuestionResponse toResponse(Question question);

    List<QuestionResponse> toResponseList(List<Question> questions);

    OptionResponse toOptionResponse(Option option);

    List<OptionResponse> toOptionResponseList(List<Option> options);
}

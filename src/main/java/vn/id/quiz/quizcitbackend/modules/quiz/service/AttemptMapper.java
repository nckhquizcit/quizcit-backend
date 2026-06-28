package vn.id.quiz.quizcitbackend.modules.quiz.service;

import java.util.List;

import org.mapstruct.Mapper;

import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.QuizResultResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.dto.quiz.StartQuizResponse;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Attempt;

@Mapper(componentModel = "spring")
public interface AttemptMapper {

    StartQuizResponse toStartQuizResponse(
            Attempt attempt);

    QuizResultResponse toQuizResultResponse(
            Attempt attempt);

    List<QuizResultResponse> toQuizResultResponseList(
            List<Attempt> attempts);
}
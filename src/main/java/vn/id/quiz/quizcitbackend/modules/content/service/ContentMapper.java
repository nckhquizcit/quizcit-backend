package vn.id.quiz.quizcitbackend.modules.content.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.ChapterResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.SubjectResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.TheoryResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Theory;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContentMapper {
    // --- Subject Mapping ---
    SubjectResponse toSubjectResponse(Subject entity);

    List<SubjectResponse> toSubjectResponseList(List<Subject> entities);

    // --- Chapter Mapping ---
    ChapterResponse toChapterResponse(Chapter entity);

    List<ChapterResponse> toChapterResponseList(List<Chapter> entities);

    // --- Theory Mapping ---
    TheoryResponse toTheoryResponse(Theory entity);
    

    List<TheoryResponse> toTheoryResponseList(List<Theory> entities);

}

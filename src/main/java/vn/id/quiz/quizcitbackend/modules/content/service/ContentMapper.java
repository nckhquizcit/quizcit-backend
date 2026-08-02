package vn.id.quiz.quizcitbackend.modules.content.service;

import org.mapstruct.Mapper;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.ChapterResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.SubjectResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.theory.TheoryResponse;
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
    default ChapterResponse toChapterResponse(Chapter entity) {
        return new ChapterResponse(entity.getChapterId(), entity.getSubjectId(), entity.getName(),
                entity.getCreatedAt(), entity.getDisplayOrder(), List.of());
    }

    default List<ChapterResponse> toChapterResponseList(List<Chapter> entities) {
        return entities.stream().map(this::toChapterResponse).toList();
    }

    // --- Theory Mapping ---
    TheoryResponse toTheoryResponse(Theory entity);
    

    List<TheoryResponse> toTheoryResponseList(List<Theory> entities);

}

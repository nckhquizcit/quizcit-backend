package vn.id.quiz.quizcitbackend.modules.content.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.ChapterResponse;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.CreateSubjectRequest;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.SubjectResponse;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.TheoryResponse;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Subject;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Theory;

@Mapper(componentModel = "spring")
public interface ContentMapper {
    SubjectResponse toResponse(Subject entity);
    @Mapping(target="subjectId", source="subject.subjectId")
    ChapterResponse toResponse(Chapter response);
    @Mapping(target="chapterId", source="chapter.chapterId")
    TheoryResponse toResponse(Theory response);
}

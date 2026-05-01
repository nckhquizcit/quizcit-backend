package vn.id.quiz.quizcitbackend.modules.content.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.ChapterResponse;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.CreateChapterRequest;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.UpdateChapterRequest;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Subject;
import vn.id.quiz.quizcitbackend.shared.utils.BaseMapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChapterMapper extends BaseMapper<Chapter, ChapterResponse, CreateChapterRequest> {
    @Mapping(target = "subject.subjectId", source = "subjectId")
    Chapter toEntity(CreateChapterRequest request);

    @Mapping(target = "subjectId", source = "subject.subjectId")
    ChapterResponse toResponse(Chapter entity);

    List<ChapterResponse> toResponseList(List<Chapter> chapters);

    @Mapping(target = "subject.subjectId", source = "subjectId", ignore = true)
    void updateEntityFromRequest(UpdateChapterRequest request, @MappingTarget Chapter entity);
}

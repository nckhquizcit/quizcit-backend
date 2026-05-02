package vn.id.quiz.quizcitbackend.modules.content.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.TheoryResponse;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.CreateTheoryRequest;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.UpdateTheoryRequest;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Theory;
import vn.id.quiz.quizcitbackend.shared.utils.BaseMapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TheoryMapper extends BaseMapper<Theory, TheoryResponse, CreateTheoryRequest> {

    @Mapping(target = "chapter.chapterId", source = "chapterId")
    Theory toEntity(CreateTheoryRequest request);

    @Mapping(target = "chapterId", source = "chapter.chapterId")
    TheoryResponse toResponse(Theory entity);

    List<TheoryResponse> toResponseList(List<Theory> theories);

    @Mapping(target = "chapter", ignore = true)
    void updateEntityFromRequest(UpdateTheoryRequest request, @MappingTarget Theory entity);
}
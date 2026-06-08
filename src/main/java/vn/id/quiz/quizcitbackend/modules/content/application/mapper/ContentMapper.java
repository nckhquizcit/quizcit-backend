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

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContentMapper {
    // --- Subject Mapping ---
    SubjectResponse toSubjectResponse(Subject entity);

    List<SubjectResponse> toSubjectResponseList(List<Subject> entities);

    // --- Chapter Mapping ---
    @Mapping(target = "subjectId", source = "subject.subjectId")
    ChapterResponse toChapterResponse(Chapter entity);

    List<ChapterResponse> toChapterResponseList(List<Chapter> entities);

    // --- Theory Mapping ---
    @Mapping(target = "chapterId", source = "chapter.chapterId")
    TheoryResponse toTheoryResponse(Theory entity);
    

    List<TheoryResponse> toTheoryResponseList(List<Theory> entities);

}

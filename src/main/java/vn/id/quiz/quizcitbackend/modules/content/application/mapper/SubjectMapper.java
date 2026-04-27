package vn.id.quiz.quizcitbackend.modules.content.application.mapper;

import org.mapstruct.Mapper;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.CreateSubjectRequest;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.SubjectResponse;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Subject;
import vn.id.quiz.quizcitbackend.shared.utils.BaseMapper;

@Mapper(componentModel = "spring")
public interface SubjectMapper extends BaseMapper<Subject, SubjectResponse, CreateSubjectRequest> {


}

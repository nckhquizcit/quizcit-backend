package vn.id.quiz.quizcitbackend.modules.content.application.service;

import vn.id.quiz.quizcitbackend.modules.content.application.dto.CreateSubjectRequest;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.SubjectResponse;

public interface SubjectService {
    SubjectResponse createSubject(CreateSubjectRequest request);
    SubjectResponse getSubject(String subjectId);

}

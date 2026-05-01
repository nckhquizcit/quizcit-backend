package vn.id.quiz.quizcitbackend.modules.content.application.service;

import vn.id.quiz.quizcitbackend.modules.content.application.dto.CreateSubjectRequest;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.SubjectResponse;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.UpdateSubjectRequest;

public interface SubjectService {
    public SubjectResponse createSubject(CreateSubjectRequest request);
    public SubjectResponse getSubject(String subjectId);
    public void deleteSubject(String subjectId);
    public SubjectResponse updateSubject(String subjectId, UpdateSubjectRequest request);
}

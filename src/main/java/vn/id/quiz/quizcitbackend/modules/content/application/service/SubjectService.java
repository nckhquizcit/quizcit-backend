package vn.id.quiz.quizcitbackend.modules.content.application.service;

import vn.id.quiz.quizcitbackend.modules.content.application.dto.CreateSubjectRequest;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.SubjectResponse;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.UpdateSubjectRequest;

import java.util.List;

public interface SubjectService {
    public List<SubjectResponse> getAllSubject();
    public SubjectResponse createSubject(CreateSubjectRequest request);
    public SubjectResponse getSubject(String subjectId);
    public void deleteSubject(String subjectId);
    public SubjectResponse updateSubject(String subjectId, UpdateSubjectRequest request);
}

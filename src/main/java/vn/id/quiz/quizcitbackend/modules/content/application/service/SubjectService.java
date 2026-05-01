package vn.id.quiz.quizcitbackend.modules.content.application.service;

import vn.id.quiz.quizcitbackend.modules.content.application.dto.CreateSubjectRequest;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.SubjectResponse;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.UpdateSubjectRequest;

import java.util.List;

public interface SubjectService {
    List<SubjectResponse> getAllSubject();
    SubjectResponse createSubject(CreateSubjectRequest request);
    SubjectResponse getSubject(String subjectId);
    void deleteSubject(String subjectId);
    SubjectResponse updateSubject(String subjectId, UpdateSubjectRequest request);
}

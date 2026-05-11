package vn.id.quiz.quizcitbackend.modules.content.application.service;

import vn.id.quiz.quizcitbackend.modules.content.application.dto.*;


public interface SubjectUseCase {
    SubjectResponse createSubject(CreateSubjectRequest request);
    ChapterResponse addChapter(String subjectId, CreateChapterRequest request);
    TheoryResponse addTheory(String subjectId, String chapterId, CreateTheoryRequest request);
}

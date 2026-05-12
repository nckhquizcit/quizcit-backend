package vn.id.quiz.quizcitbackend.modules.content.application.service;

import vn.id.quiz.quizcitbackend.modules.content.application.dto.*;

import java.util.List;


public interface SubjectUseCase {
    List<SubjectResponse> getAllSubject();
    SubjectResponse getSubjectById(String subjectId);
    ChapterResponse getChapterById(String chapterId);
    TheoryResponse getTheoryById(String theoryId);
    SubjectResponse createSubject(CreateSubjectRequest request);
    ChapterResponse addChapter(String subjectId, CreateChapterRequest request);
    TheoryResponse addTheory(String subjectId, String chapterId, CreateTheoryRequest request);
    void deleteSubject(String subjectId);
    List<TheoryResponse> getAllTheoriesFromSubject(String subjectId);
    List<TheoryResponse> getAllTheories(String subjectId, String chapterId);
    List<ChapterResponse> getAllChapters(String subjectId);
    void updateSubject(String subjectId, UpdateSubjectRequest request);
    void updateChapter(String subjectId, String chapterId, UpdateChapterRequest request);
    void updateTheory(String subjectId, String chapterId, String theoryId, UpdateTheoryRequest request);
    void deleteChapter(String subjectId, String chapterId);
    void deleteTheory(String subjectId, String chapterId, String theoryId);
}

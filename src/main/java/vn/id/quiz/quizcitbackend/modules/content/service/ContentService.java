package vn.id.quiz.quizcitbackend.modules.content.service;

import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.*;

import java.util.List;


public interface ContentService {
    List<SubjectResponse> getAllSubject();
    SubjectResponse getSubjectById(String subjectId);
    ChapterResponse getChapterById(String chapterId);
    TheoryResponse getTheoryById(String theoryId);
    SubjectResponse createSubject(CreateSubjectRequest request);
    ChapterResponse addChapter(String subjectId, CreateChapterRequest request);
    TheoryResponse addTheory(String chapterId, CreateTheoryRequest request);
    void deleteSubject(String subjectId);
    List<TheoryResponse> getAllTheories(String chapterId);
    List<ChapterResponse> getAllChapters(String subjectId);
    void updateSubject(String subjectId, UpdateSubjectRequest request);
    void updateChapter(String chapterId, UpdateChapterRequest request);
    void updateTheory(String theoryId, UpdateTheoryRequest request);
    void deleteChapter(String chapterId);
    void deleteTheory(String theoryId);
}

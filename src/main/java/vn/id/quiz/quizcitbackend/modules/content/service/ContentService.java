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

    /**
     * Phase 2 (P1): lấy danh sách đề thi thuộc về một môn học.
     */
    List<QuizSummaryResponse> getQuizzesBySubject(String subjectId);

    void updateSubject(String subjectId, UpdateSubjectRequest request);
    void updateChapter(String chapterId, UpdateChapterRequest request);
    void updateTheory(String theoryId, UpdateTheoryRequest request);
    void deleteChapter(String chapterId);
    void deleteTheory(String theoryId);
}

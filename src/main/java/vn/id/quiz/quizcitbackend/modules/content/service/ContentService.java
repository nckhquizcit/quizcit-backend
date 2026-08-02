package vn.id.quiz.quizcitbackend.modules.content.service;

import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.*;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.ChapterResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.CreateChapterRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.UpdateChapterRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.CreateSubjectRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.SubjectResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.UpdateSubjectRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.theory.CreateTheoryRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.theory.TheoryResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.theory.UpdateTheoryRequest;

import java.util.List;
import vn.id.quiz.quizcitbackend.shared.dto.ReorderRequest;


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
    List<TheoryResponse> getAllTheoriesBySubject(String subjectId);
    List<ChapterResponse> getAllChapters(String subjectId);
    List<ChapterResponse> getAllAccessibleChapters();

    /**
     * Phase 2 (P1): lấy danh sách đề thi thuộc về một môn học.
     */
    List<QuizSummaryResponse> getQuizzesBySubject(String subjectId);

    void updateSubject(String subjectId, UpdateSubjectRequest request);
    void updateChapter(String chapterId, UpdateChapterRequest request);
    void updateTheory(String theoryId, UpdateTheoryRequest request);
    void deleteChapter(String chapterId);
    void deleteTheory(String theoryId);
    void reorderSubjects(ReorderRequest request);
    void reorderChapters(String subjectId, ReorderRequest request);
}

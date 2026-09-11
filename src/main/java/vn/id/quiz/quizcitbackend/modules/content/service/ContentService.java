package vn.id.quiz.quizcitbackend.modules.content.service;

import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.*;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.SubjectCatalogSummaryResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.SubjectStudyOverviewResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.theory.TheoryReaderViewResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.ChapterEditContextResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.ChapterResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.ChapterSummaryResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.CreateChapterRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.FlatChapterResponse;
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
    ChapterEditContextResponse getChapterEditContext(String chapterId);
    TheoryResponse getTheoryById(String theoryId);
    SubjectResponse createSubject(CreateSubjectRequest request);
    ChapterResponse addChapter(String subjectId, CreateChapterRequest request);
    TheoryResponse addTheory(String chapterId, CreateTheoryRequest request);
    void deleteSubject(String subjectId);
    void hardDeleteSubject(String subjectId);
    List<TheoryResponse> getAllTheories(String chapterId);
    List<TheoryResponse> getAllTheoriesBySubject(String subjectId);
    int reindexTheoriesBySubject(String subjectId);
    List<ChapterResponse> getAllChapters(String subjectId);
    List<ChapterSummaryResponse> getChaptersSummary(String subjectId);
    List<FlatChapterResponse> getAllFlatChapters();
    List<ChapterResponse> getAllAccessibleChapters();

    /**
     * Phase 2 (P1): lấy danh sách đề thi thuộc về một môn học.
     */
    List<QuizSummaryResponse> getQuizzesBySubject(String subjectId);

    /**
     * Phase 3: Tổng quan không gian học tập của môn học (môn, trạng thái đăng ký, cây chương, lý thuyết, quiz, kỹ năng).
     */
    SubjectStudyOverviewResponse getSubjectStudyOverview(String subjectId);

    /**
     * Phase 3: Nội dung chi tiết bài học lý thuyết kèm danh sách bài học cùng chương để render điều hướng bài đọc.
     */
    TheoryReaderViewResponse getTheoryReaderView(String theoryId);

    /**
     * Phase 4: Danh mục môn học tổng hợp kèm số chương đã mở, tổng chương và trạng thái đăng ký của sinh viên.
     */
    List<SubjectCatalogSummaryResponse> getSubjectCatalogSummary();

    void updateSubject(String subjectId, UpdateSubjectRequest request);
    void updateChapter(String chapterId, UpdateChapterRequest request);
    void updateTheory(String theoryId, UpdateTheoryRequest request);
    void deleteChapter(String chapterId);
    void hardDeleteChapter(String chapterId);
    void deleteTheory(String theoryId);
    void hardDeleteTheory(String theoryId);
    void reorderSubjects(ReorderRequest request);
    void reorderChapters(String subjectId, ReorderRequest request);
}

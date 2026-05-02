package vn.id.quiz.quizcitbackend.modules.content.application.service;

import vn.id.quiz.quizcitbackend.modules.content.application.dto.ChapterResponse;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.CreateChapterRequest;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.UpdateChapterRequest;

import java.util.List;

public interface ChapterService {
    List<ChapterResponse> getAllChapters();
    /**
     * Chuyển các chương từ học phần này sang học phần khác
     * @param sourceSubject: Id của học phần ban đầu
     * @param targetSubject: Id của học phần muốn chuyển đến
     * @return Danh sách các chương sau khi đã chuyển sang học phần khác
     */
    List<ChapterResponse> changeSubject(String sourceSubject, String targetSubject);
    ChapterResponse createChapter(CreateChapterRequest request);
    ChapterResponse getChapter(String chapterId);
    ChapterResponse updateChapter(String chapterId, UpdateChapterRequest request);
    void deleteChapter(String chapterId);
}

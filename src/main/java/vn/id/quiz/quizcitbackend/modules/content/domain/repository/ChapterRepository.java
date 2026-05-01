package vn.id.quiz.quizcitbackend.modules.content.domain.repository;

import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Chapter;

import java.util.List;
import java.util.Optional;

public interface ChapterRepository {
    Optional<Chapter> findById(String id);
    List<Chapter> findAll();

    /**
     * Chuyển các chương từ học phần này sang học phần khác
     * @param sourceSubject: Id của học phần ban đầu
     * @param targetSubject: Id của học phần muốn chuyển đến
     * @return Danh sách các chương sau khi đã chuyển sang học phần khác
     */
    List<Chapter> changeSubject(String sourceSubject, String targetSubject);
    Chapter save(Chapter chapter);
    void deleteById(String id);
    boolean existsById(String id);
}

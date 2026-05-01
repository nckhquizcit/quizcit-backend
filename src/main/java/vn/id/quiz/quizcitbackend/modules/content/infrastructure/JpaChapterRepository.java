package vn.id.quiz.quizcitbackend.modules.content.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Subject;
import vn.id.quiz.quizcitbackend.modules.content.domain.repository.ChapterRepository;

import java.util.List;

@Repository
public interface JpaChapterRepository extends JpaRepository<Chapter, String>, ChapterRepository {
    @Override
    @Transactional
    default List<Chapter> changeSubject(String sourceSubject, String targetSubject) {
        // 1. Tìm các chương thuộc subject cũ
        // Bạn cần định nghĩa thêm hàm findBySubjectSubjectId trong JpaRepository
        List<Chapter> chapters = findBySubjectSubjectId(sourceSubject);

        // 2. Cập nhật ID mới cho từng chương (Dirty Checking sẽ lo phần SQL UPDATE)
        chapters.forEach(chapter -> {
            // Giả sử Chapter có field Subject, bạn cần set lại Subject object hoặc ID
            Subject newSubject = Subject.builder()
                    .subjectId(targetSubject)
                    .build();
            chapter.setSubject(newSubject);
        });

        return chapters; // Trả về danh sách đã đổi (Hibernate tự flush xuống DB)
    }
    //Access đến thộc tính SubjectId của field Subject của Chapter
    List<Chapter> findBySubjectSubjectId(String subjectId);
}

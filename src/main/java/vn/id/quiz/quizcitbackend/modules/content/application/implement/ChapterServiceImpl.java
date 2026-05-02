package vn.id.quiz.quizcitbackend.modules.content.application.implement;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.ChapterResponse;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.CreateChapterRequest;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.UpdateChapterRequest;
import vn.id.quiz.quizcitbackend.modules.content.application.mapper.ChapterMapper;
import vn.id.quiz.quizcitbackend.modules.content.application.service.ChapterService;
import vn.id.quiz.quizcitbackend.modules.content.application.service.SubjectService;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.domain.repository.ChapterRepository;
import vn.id.quiz.quizcitbackend.modules.content.domain.repository.SubjectRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChapterServiceImpl implements ChapterService {
    private final ChapterRepository repository;
    private final SubjectRepository subjectRepository;
    private final ChapterMapper mapper;

    @Override
    public List<ChapterResponse> getAllChapters() {
        List<Chapter> chapters = repository.findAll();
        return mapper.toResponseList(chapters);
    }

    /**
     * Chuyển các chương từ học phần này sang học phần khác
     *
     * @param sourceSubject : Id của học phần ban đầu
     * @param targetSubject : Id của học phần muốn chuyển đến
     * @return Danh sách các chương sau khi đã chuyển sang học phần khác
     */
    @Transactional
    @Override
    public List<ChapterResponse> changeSubject(String sourceSubject, String targetSubject) {
        if(!subjectRepository.existsById(targetSubject)){
            throw new AppException("Không tồn tại học phần đích.",HttpStatus.NOT_FOUND);
        }
        List<Chapter> chapters = repository.changeSubject(sourceSubject, targetSubject);
        return mapper.toResponseList(chapters);
    }
    @Transactional
    @Override
    public ChapterResponse createChapter(CreateChapterRequest request) {
        if(!subjectRepository.existsById(request.subjectId())){
            throw new AppException("Không tồn tại học phần mà chương này thuộc về.", HttpStatus.NOT_FOUND);
        }
        Chapter entity = mapper.toEntity(request);

        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public ChapterResponse getChapter(String chapterId) {
        Chapter response = repository.findById(chapterId).orElseThrow(()->{
            return new AppException("Không tìm thấy chương được yêu cầu", HttpStatus.NOT_FOUND);
        });
        return mapper.toResponse(response);

    }
    @Transactional
    @Override
    public ChapterResponse updateChapter(String chapterId, UpdateChapterRequest request) {
        if(!subjectRepository.existsById(request.subjectId())){
            throw new AppException("Không tìm thấy học phần mà chương này thuộc về.", HttpStatus.NOT_FOUND);
        }
        Chapter chapter = repository.findById(chapterId).orElseThrow(()->{
            return new AppException("Không tìm thấy chương được yêu cầu", HttpStatus.NOT_FOUND);
        });
        // 1. Update các field cơ bản (name, ...)
        mapper.updateEntityFromRequest(request, chapter);

        // 2. Cập nhật quan hệ bằng Proxy (Tối ưu nhất)
        // Thay vì tạo 'new Subject' thủ công, ta dùng Proxy của Hibernate
        chapter.setSubject(subjectRepository.getReferenceById(request.subjectId()));
        return mapper.toResponse(repository.save(chapter));

    }
    @Transactional
    @Override
    public void deleteChapter(String chapterId) {
        if(!repository.existsById(chapterId)){
            throw new AppException("Không tìm thấy chương được yêu cầu", HttpStatus.NOT_FOUND);
        }
        repository.deleteById(chapterId);
    }
}

package vn.id.quiz.quizcitbackend.modules.content.application.implement;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.CreateTheoryRequest;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.TheoryResponse;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.UpdateTheoryRequest;
import vn.id.quiz.quizcitbackend.modules.content.application.mapper.TheoryMapper;
import vn.id.quiz.quizcitbackend.modules.content.application.service.TheoryService;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Theory;
import vn.id.quiz.quizcitbackend.modules.content.domain.repository.ChapterRepository;
import vn.id.quiz.quizcitbackend.modules.content.domain.repository.TheoryRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TheoryServiceImpl implements TheoryService {
    private final TheoryRepository repository;
    private final ChapterRepository chapterRepository;
    private final TheoryMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<TheoryResponse> getAllTheories() {
        List<Theory> theories = repository.findAll();
        return mapper.toResponseList(theories);
    }

    @Override
    @Transactional(readOnly = true)
    public TheoryResponse getTheory(String theoryId) {
        Theory theory = repository.findById(theoryId).orElseThrow(() ->
                new AppException("Không tìm thấy nội dung lý thuyết", HttpStatus.NOT_FOUND));
        return mapper.toResponse(theory);
    }

    @Transactional
    @Override
    public TheoryResponse createTheory(CreateTheoryRequest request) {
        if (!chapterRepository.existsById(request.chapterId())) {
            throw new AppException("Chương được chọn không tồn tại", HttpStatus.NOT_FOUND);
        }

        Theory entity = mapper.toEntity(request);
        // MapStruct đã lo việc set chapterId vào vỏ chapter Entity qua annotation @Mapping

        return mapper.toResponse(repository.save(entity));
    }

    @Transactional
    @Override
    public TheoryResponse updateTheory(String theoryId, UpdateTheoryRequest request) {
        Theory theory = repository.findById(theoryId).orElseThrow(() ->
                new AppException("Không tìm thấy nội dung lý thuyết để cập nhật", HttpStatus.NOT_FOUND));

        // Nếu muốn đổi chapter của theory
        if (request.chapterId() != null && !chapterRepository.existsById(request.chapterId())) {
            throw new AppException("Chương mới không tồn tại", HttpStatus.NOT_FOUND);
        }

        mapper.updateEntityFromRequest(request, theory);

        // Gán lại chapter nếu có thay đổi (sử dụng logic tương tự Chapter)
        if (request.chapterId() != null) {
            theory.setChapter(chapterRepository.getReferenceById(request.chapterId()));
        }

        return mapper.toResponse(repository.save(theory));
    }

    @Transactional
    @Override
    public void deleteTheory(String theoryId) {
        if (!repository.existsById(theoryId)) {
            throw new AppException("Không tìm thấy nội dung lý thuyết để xóa", HttpStatus.NOT_FOUND);
        }
        repository.deleteById(theoryId);
    }
}
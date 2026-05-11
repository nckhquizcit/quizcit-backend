package vn.id.quiz.quizcitbackend.modules.content.application.implement;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.*;
import vn.id.quiz.quizcitbackend.modules.content.application.mapper.ContentMapper;
import vn.id.quiz.quizcitbackend.modules.content.application.service.SubjectUseCase;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Subject;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Theory;
import vn.id.quiz.quizcitbackend.modules.content.domain.repository.SubjectRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectUseCase {
    private final SubjectRepository subjectRepository;
    private final ContentMapper mapper;


    @Override
    @Transactional
    public SubjectResponse createSubject(CreateSubjectRequest request){
        if(subjectRepository.existByCourseCode(request.courseCode())){
            throw new AppException("Mã học phần đã tồn tại.", HttpStatus.BAD_REQUEST);
        }
        Subject subject = Subject.create(request.courseCode(), request.name(), request.description());
        return mapper.toResponse(subjectRepository.save(subject));
    }

    @Override
    @Transactional
    public ChapterResponse addChapter(String subjectId, CreateChapterRequest request){
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(()-> new AppException("Không tìm thấy học phần được yêu cầu", HttpStatus.BAD_REQUEST));
        Chapter response = subject.addChapter(request.name());
        subjectRepository.save(subject);
        return mapper.toResponse(response);
    }
    @Override
    @Transactional
    public TheoryResponse addTheory(String subjectId, String chapterId, CreateTheoryRequest request){
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(()-> new AppException("Không tìm thấy học phần được yêu cầu", HttpStatus.BAD_REQUEST));
        Theory response = subject.addTheory(chapterId, request.content());
        subjectRepository.save(subject);
        return mapper.toResponse(response);
    }

}

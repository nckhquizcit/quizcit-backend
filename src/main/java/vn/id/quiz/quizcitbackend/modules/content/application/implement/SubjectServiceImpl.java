package vn.id.quiz.quizcitbackend.modules.content.application.implement;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.CreateSubjectRequest;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.SubjectResponse;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.UpdateSubjectRequest;
import vn.id.quiz.quizcitbackend.modules.content.application.mapper.SubjectMapper;
import vn.id.quiz.quizcitbackend.modules.content.application.service.SubjectService;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Subject;
import vn.id.quiz.quizcitbackend.modules.content.domain.repository.SubjectRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {
    private final SubjectRepository repository;
    private final SubjectMapper mapper;

    @Transactional
    @Override
    public SubjectResponse createSubject(CreateSubjectRequest request){
        Subject subject = mapper.toEntity(request);
        return mapper.toResponse(repository.save(subject));
    }
    @Override
    public SubjectResponse getSubject(String subjectId){
        Subject subject = repository.findById(subjectId).orElseThrow(()-> new AppException("Không tìm thấy học phần được yêu cầu.", HttpStatus.NOT_FOUND));
        return mapper.toResponse(subject);
    }
    @Override
    public void deleteSubject(String subjectId){
        if(!repository.existsById(subjectId)){
            throw new AppException("Không tìm thấy học phần được yêu cầu.", HttpStatus.NOT_FOUND);
        }
        repository.deleteById(subjectId);
    }
    @Override
    public SubjectResponse updateSubject(String subjectId, UpdateSubjectRequest request){
        Subject subject = repository.findById(subjectId).orElseThrow(()-> new AppException("Không tìm thấy học phần được yêu cầu.", HttpStatus.NOT_FOUND));
        mapper.updateEntityFromRequest(request, subject);
        return mapper.toResponse(repository.save(subject));

    }
}

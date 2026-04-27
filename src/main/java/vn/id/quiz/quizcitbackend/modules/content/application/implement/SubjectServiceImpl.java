package vn.id.quiz.quizcitbackend.modules.content.application.implement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.CreateSubjectRequest;
import vn.id.quiz.quizcitbackend.modules.content.application.dto.SubjectResponse;
import vn.id.quiz.quizcitbackend.modules.content.application.mapper.SubjectMapper;
import vn.id.quiz.quizcitbackend.modules.content.application.service.SubjectService;
import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Subject;
import vn.id.quiz.quizcitbackend.modules.content.domain.repository.SubjectRepository;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {
    private final SubjectRepository repository;
    private final SubjectMapper mapper;

    @Transactional
    @Override
    public SubjectResponse createSubject(CreateSubjectRequest request){
        Subject subject = mapper.toEntity(request);
        repository.save(subject);
        return mapper.toResponse(subject);
    }
}

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

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectUseCase {
    private final SubjectRepository subjectRepository;
    private final ContentMapper mapper;

    @Override
    public List<SubjectResponse> getAllSubject(){
        List<Subject> subjects = subjectRepository.findAll();
        return mapper.toSubjectResponseList(subjects);
    }
    @Override
    public SubjectResponse getSubjectById(String subjectId){

        Subject subject = subjectRepository.findSubjectById(subjectId)
                .orElseThrow(()-> new AppException("Không tìm thấy học phần được yêu cầu", HttpStatus.NOT_FOUND));
        return mapper.toSubjectResponse(subject);
    }
    @Override
    public ChapterResponse getChapterById(String chapterId){
        Chapter chapter = subjectRepository.findChapterById(chapterId)
                .orElseThrow(()-> new AppException("Không tìm thấy chương được yêu cầu", HttpStatus.NOT_FOUND));
        return mapper.toChapterResponse(chapter);
    }
    @Override
    public TheoryResponse getTheoryById(String theoryId){
        Theory theory = subjectRepository.findTheoryById(theoryId)
                .orElseThrow(()-> new AppException("Không tìm thấy nội dung lý thuyết được yêu cầu", HttpStatus.NOT_FOUND));
        return mapper.toTheoryResponse(theory);
    }

    @Override
    @Transactional
    public SubjectResponse createSubject(CreateSubjectRequest request){

        if(subjectRepository.existsByCourseCode(request.courseCode())){
            throw new AppException("Mã học phần đã tồn tại.", HttpStatus.CONFLICT);
        }
        Subject subject = Subject.create(request.courseCode(), request.name(), request.description());
        return mapper.toSubjectResponse(subjectRepository.save(subject));
    }

    @Override
    @Transactional
    public ChapterResponse addChapter(String subjectId, CreateChapterRequest request){
        Subject subject = subjectRepository.findSubjectById(subjectId)
                .orElseThrow(()-> new AppException("Không tìm thấy học phần được yêu cầu", HttpStatus.NOT_FOUND));
        Chapter response = subject.addChapter(request.name());
        subjectRepository.save(subject);
        return mapper.toChapterResponse(response);
    }
    @Override
    @Transactional
    public TheoryResponse addTheory(String subjectId, String chapterId, CreateTheoryRequest request){
        Subject subject = subjectRepository.findSubjectById(subjectId)
                .orElseThrow(()-> new AppException("Không tìm thấy học phần được yêu cầu", HttpStatus.NOT_FOUND));
        Theory response = subject.addTheory(chapterId, request.content());
        subjectRepository.save(subject);
        return mapper.toTheoryResponse(response);
    }

    @Override
    @Transactional
    public void deleteSubject(String subjectId){
        if(!subjectRepository.existsById(subjectId)){
            throw new AppException("Không tìm thấy học phần được yêu cầu", HttpStatus.NOT_FOUND);
        }
        subjectRepository.deleteById(subjectId);
    }
    @Override
    public List<ChapterResponse> getAllChapters(String subjectId){
        Subject subject = subjectRepository.findSubjectById(subjectId)
                .orElseThrow(()->new AppException("Không tìm thấy học phần được yêu cầu", HttpStatus.NOT_FOUND));
        List<Chapter> chapters = subject.getAllChapters();
        return mapper.toChapterResponseList(chapters);
    }
    @Deprecated //cai nay la tan du che do cu, de lai cung dc, biet dau tuong lai co xai
    @Override
    public List<TheoryResponse> getAllTheoriesFromSubject(String subjectId){
        if(!subjectRepository.existsById(subjectId)){
            throw new AppException("Không tìm thấy học phần được yêu cầu", HttpStatus.NOT_FOUND);
        }
        List<Theory> theories = subjectRepository.findAllTheoriesBySubjectId(subjectId);
        return mapper.toTheoryResponseList(theories);

    }
    @Override
    public List<TheoryResponse> getAllTheories(String subjectId, String chapterId){
        Subject subject = subjectRepository.findSubjectById(subjectId)
                .orElseThrow(()-> new AppException("Không tìm thấy học phần được yêu cầu", HttpStatus.NOT_FOUND));
        List<Theory> theories = subject.getAllTheories(chapterId);
        return mapper.toTheoryResponseList(theories);
    }
    @Transactional
    @Override
    public void updateSubject(String subjectId, UpdateSubjectRequest request){
        Subject subject = subjectRepository.findSubjectById(subjectId)
                .orElseThrow(()-> new AppException("Không tìm thấy học phần được yêu cầu", HttpStatus.NOT_FOUND));
        subject.updateSubject(request.courseCode(), request.name(), request.description());
        subjectRepository.save(subject);
    }
    @Transactional
    @Override
    public void updateChapter(String subjectId, String chapterId, UpdateChapterRequest request){
        Subject subject = subjectRepository.findSubjectById(subjectId)
                .orElseThrow(()-> new AppException("Không tìm thấy học phần được yêu cầu", HttpStatus.NOT_FOUND));
        subject.updateChapter(chapterId, request.name());
        subjectRepository.save(subject);
    }
    @Transactional
    @Override
    public void updateTheory(String subjectId, String chapterId, String theoryId, UpdateTheoryRequest request){
        Subject subject = subjectRepository.findSubjectById(subjectId)
                .orElseThrow(()-> new AppException("Không tìm thấy học phần được yêu cầu", HttpStatus.NOT_FOUND));
        subject.updateTheory(chapterId,theoryId, request.content());
        subjectRepository.save(subject);
    }

    @Override
    @Transactional
    public void deleteChapter(String subjectId, String chapterId) {
        Subject subject = subjectRepository.findSubjectById(subjectId)
                .orElseThrow(()-> new AppException("Không tìm thấy học phần được yêu cầu", HttpStatus.NOT_FOUND));
        subject.deleteChapter(chapterId);
        subjectRepository.save(subject);

    }

    @Override
    @Transactional
    public void deleteTheory(String subjectId, String chapterId, String theoryId) {
        Subject subject = subjectRepository.findSubjectById(subjectId)
                .orElseThrow(()-> new AppException("Không tìm thấy học phần được yêu cầu", HttpStatus.NOT_FOUND));
        subject.deleteTheory(chapterId,theoryId);
        subjectRepository.save(subject);

    }
}

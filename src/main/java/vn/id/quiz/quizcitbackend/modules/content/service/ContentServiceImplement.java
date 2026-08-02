package vn.id.quiz.quizcitbackend.modules.content.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.ChapterResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.CreateChapterRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.UpdateChapterRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.CreateSubjectRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.SubjectResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.UpdateSubjectRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.theory.CreateTheoryRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.theory.TheoryResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.theory.UpdateTheoryRequest;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Subject;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Theory;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.*;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentServiceImplement implements ContentService {
    private final ContentRepository contentRepository;
    private final ContentMapper mapper;
    private final QuizRepository quizRepository;
    private final SubjectVisibilityGuard visibilityGuard;

    // =========================================================================
    // Phase 2 (P1): Lấy danh sách đề thi thuộc về 1 môn học
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public List<QuizSummaryResponse> getQuizzesBySubject(String subjectId) {
        visibilityGuard.requireVisibleSubject(subjectId);
        if (!contentRepository.existsBySubjectId(subjectId)) {
            throw new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        List<Quiz> quizzes = quizRepository.findQuizzesBySubjectId(subjectId);
        return quizzes.stream()
                .map(QuizSummaryResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> getAllSubject(){
        List<Subject> subjects = visibilityGuard.isAdministrator()
                ? contentRepository.findAllSubject()
                : contentRepository.findAllVisibleSubject();
        return mapper.toSubjectResponseList(subjects);
    }
    @Override
    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(String subjectId){
        visibilityGuard.requireVisibleSubject(subjectId);
        Subject subject = contentRepository.findSubjectById(subjectId)
                .orElseThrow(()-> new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND));
        return mapper.toSubjectResponse(subject);
    }
    @Override
    @Transactional(readOnly = true)
    public ChapterResponse getChapterById(String chapterId){
        visibilityGuard.requireVisibleChapter(chapterId);
        Chapter chapter = contentRepository.findChapterById(chapterId)
                .orElseThrow(()-> new AppException(ErrorMessages.CHAPTER_NOT_FOUND, HttpStatus.NOT_FOUND));
        return mapper.toChapterResponse(chapter);
    }
    @Override
    @Transactional(readOnly = true)
    public TheoryResponse getTheoryById(String theoryId){
        visibilityGuard.requireVisibleTheory(theoryId);
        Theory theory = contentRepository.findTheoryById(theoryId)
                .orElseThrow(()-> new AppException("Không tìm thấy nội dung lý thuyết được yêu cầu", HttpStatus.NOT_FOUND));
        return mapper.toTheoryResponse(theory);
    }

    @Override
    @Transactional
    public SubjectResponse createSubject(CreateSubjectRequest request){

        if(contentRepository.existsByCourseCode(request.courseCode())){
            throw new AppException("Mã học phần đã tồn tại.", HttpStatus.CONFLICT);
        }
        Subject subject = Subject.create(request.courseCode(), request.name(), request.description());
        return mapper.toSubjectResponse(contentRepository.save(subject));
    }

    @Override
    @Transactional
    public ChapterResponse addChapter(String subjectId, CreateChapterRequest request){
        if(!contentRepository.existsBySubjectId(subjectId)){
            throw new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        Chapter response = Chapter.create(subjectId, request.name());
        contentRepository.save(response);
        return mapper.toChapterResponse(response);
    }
    @Override
    @Transactional
    public TheoryResponse addTheory(String chapterId, CreateTheoryRequest request){
        if(!contentRepository.existsByChapterId(chapterId)){
            throw new AppException(ErrorMessages.CHAPTER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        Theory response = Theory.create(chapterId, request.content(), request.contentFormat());
        contentRepository.save(response);
        return mapper.toTheoryResponse(response);
    }


    @Override
    @Transactional(readOnly = true)
    public List<ChapterResponse> getAllChapters(String subjectId){
        visibilityGuard.requireVisibleSubject(subjectId);
        if(!contentRepository.existsBySubjectId(subjectId)){
            throw new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        List<Chapter> chapters = contentRepository.findAllChapter(subjectId);
        return mapper.toChapterResponseList(chapters);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TheoryResponse> getAllTheories(String chapterId){
        visibilityGuard.requireVisibleChapter(chapterId);
        if(!contentRepository.existsByChapterId(chapterId)){
            throw new AppException(ErrorMessages.CHAPTER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        List<Theory> theories = contentRepository.findAllTheory(chapterId);
        return mapper.toTheoryResponseList(theories);
    }
    @Transactional
    @Override
    public void updateSubject(String subjectId, UpdateSubjectRequest request){
        Subject subject = contentRepository.findSubjectById(subjectId)
                .orElseThrow(()-> new AppException(ErrorMessages.SUBJECT_NOT_FOUND, HttpStatus.NOT_FOUND));
        subject.updateSubject(request.courseCode(), request.name(), request.description(), request.visible());
        contentRepository.save(subject);
    }
    @Transactional
    @Override
    public void updateChapter(String chapterId, UpdateChapterRequest request){
        Chapter chapter = contentRepository.findChapterById(chapterId)
                        .orElseThrow(()->new AppException(ErrorMessages.CHAPTER_NOT_FOUND, HttpStatus.NOT_FOUND));

        chapter.updateChapter(request.name());
        contentRepository.save(chapter);
    }
    @Transactional
    @Override
    public void updateTheory(String theoryId, UpdateTheoryRequest request){
        Theory theory = contentRepository.findTheoryById(theoryId)
                .orElseThrow(()-> new AppException("Không tìm thấy nội dung lý thuyết được yêu cầu", HttpStatus.NOT_FOUND));
        theory.updateTheory(request.content(), request.contentFormat());
        contentRepository.save(theory);
    }

    @Override
    @Transactional
    public void deleteSubject(String subjectId){
        contentRepository.deleteSubjectById(subjectId);
    }
    @Override
    @Transactional
    public void deleteChapter(String chapterId) {
        contentRepository.deleteChapterById(chapterId);
    }

    @Override
    @Transactional
    public void deleteTheory(String theoryId) {
        contentRepository.deleteTheoryById(theoryId);
    }
}

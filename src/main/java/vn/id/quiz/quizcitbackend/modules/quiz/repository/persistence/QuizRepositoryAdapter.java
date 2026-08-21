package vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.QuestionSkill;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Attempt;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Option;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizQuestion;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Response;
import vn.id.quiz.quizcitbackend.modules.quiz.repository.QuizRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QuizRepositoryAdapter implements QuizRepository {

    private final JpaQuestionRepository jpaQuestionRepository;
    private final JpaQuizRepository jpaQuizRepository;
    private final JpaQuizQuestionRepository jpaQuizQuestionRepository;
    private final JpaOptionRepository jpaOptionRepository;
    private final JpaAttemptRepository jpaAttemptRepository;
    private final JpaResponseRepository jpaResponseRepository;
    private final vn.id.quiz.quizcitbackend.modules.progress.repository.persistence.JpaQuestionSkillRepository jpaQuestionSkillRepository;

    // Question
    @Override
    public List<Question> findQuestionsByChapterId(String chapterId) {
        return jpaQuestionRepository.findByChapterId(chapterId);
    }

    @Override
    public List<Question> findQuestionsByChapterIds(java.util.Collection<String> chapterIds) {
        return chapterIds == null || chapterIds.isEmpty() ? List.of() : jpaQuestionRepository.findByChapterIdIn(chapterIds);
    }

    @Override
    public List<Question> findAllQuestions() {
        return jpaQuestionRepository.findAll();
    }

    @Override
    public Question save(Question question) {
        return jpaQuestionRepository.save(question);
    }

    @Override
    public Optional<Question> findQuestionById(
            String questionId) {

        return jpaQuestionRepository
                .findByQuestionId(questionId);
    }

    @Override
    public List<Question> findQuestionsByIds(
            java.util.Collection<String> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return List.of();
        }
        return jpaQuestionRepository.findByQuestionIdIn(questionIds);
    }


    @Override
    public void deleteQuestionById(
            String questionId) {
        softDeleteQuestionById(questionId);
    }

    @Override
    public void softDeleteQuestionById(String questionId) {
        jpaQuestionRepository.findByQuestionId(questionId).ifPresent(q -> {
            q.softDelete();
            jpaQuestionRepository.save(q);
        });
    }

    @Override
    public void hardDeleteQuestionById(String questionId) {
        deleteResponsesByQuestionId(questionId);
        deleteQuizQuestionsByQuestionId(questionId);
        deleteQuestionSkillsByQuestionId(questionId);
        deleteOptionsByQuestionId(questionId);
        jpaQuestionRepository.deleteByQuestionId(questionId);
    }

    // Quiz
    @Override
    public Quiz save(Quiz quiz) {
        return jpaQuizRepository.save(quiz);
    }

    @Override
    public QuizQuestion save(QuizQuestion quizQuestion) {
        return jpaQuizQuestionRepository.save(quizQuestion);
    }

    @Override
    public List<QuizQuestion> findQuizQuestionsByQuizId(String quizId) {
        return jpaQuizQuestionRepository.findByQuizIdOrderByIndexOrder(quizId);
    }

    @Override
    public void deleteQuizQuestionsByQuestionId(String questionId) {
        jpaQuizQuestionRepository.deleteByQuestionId(questionId);
    }

    @Override
    public Optional<Quiz> findQuizById(
            String quizId) {

        return jpaQuizRepository
                .findByQuizId(quizId);
    }

    @Override
    public List<Quiz> findQuizzesByChapterId(
            String chapterId) {

        return jpaQuizRepository
                .findByChapterIdOrderByDisplayOrderAscCreatedAtAscQuizIdAsc(chapterId);
    }

    @Override
    public int nextQuizDisplayOrder(String chapterId) {
        Quiz last = jpaQuizRepository.findTopByChapterIdOrderByDisplayOrderDesc(chapterId);
        return last == null ? 1 : last.getDisplayOrder() + 1;
    }

    // Phase 2 (P1): danh sách đề của một môn học
    @Override
    public List<Quiz> findQuizzesBySubjectId(String subjectId) {
        return jpaQuizRepository.findBySubjectIdOrderByDisplayOrderAscCreatedAtAscQuizIdAsc(subjectId);
    }

    @Override
    public List<Quiz> findNonAdaptiveQuizzesBySubjectId(String subjectId) {
        return jpaQuizRepository.findBySubjectIdAndTypeNotOrderByDisplayOrderAscCreatedAtAscQuizIdAsc(
                subjectId, vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizType.ADAPTIVE);
    }

    @Override
    public List<Quiz> findQuizzesByIds(java.util.Collection<String> quizIds) {
        return quizIds == null || quizIds.isEmpty() ? List.of() : jpaQuizRepository.findAllById(quizIds);
    }

    @Override
    public void deleteQuizById(
            String quizId) {
        softDeleteQuizById(quizId);
    }

    @Override
    public void softDeleteQuizById(String quizId) {
        jpaQuizRepository.findByQuizId(quizId).ifPresent(quiz -> {
            quiz.softDelete();
            jpaQuizRepository.save(quiz);
        });
    }

    @Override
    public void hardDeleteQuizById(String quizId) {
        deleteQuizQuestionsByQuizId(quizId);
        jpaQuizRepository.deleteByQuizId(quizId);
    }

    @Override
    public boolean existsAttemptByQuizId(String quizId) {
        return jpaAttemptRepository.existsByQuizId(quizId);
    }

    // Option
    @Override
    public Option save(Option option) {
        return jpaOptionRepository.save(option);
    }

    @Override
    public List<Option> findOptionsByQuestionId(String questionId) {
        return jpaOptionRepository.findByQuestionId(questionId);
    }

    @Override
    public List<Option> findOptionsByQuestionIds(
            java.util.Collection<String> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return List.of();
        }
        return jpaOptionRepository.findByQuestionIdIn(questionIds);
    }


    @Override
    public void deleteOptionsByQuestionId(String questionId) {
        jpaOptionRepository.deleteByQuestionId(questionId);
    }

    @Override
    public Optional<Option> findOptionById(
            String optionId) {

        return jpaOptionRepository
                .findByOptionId(optionId);
    }

    // Attempt
    @Override
    public Attempt save(Attempt attempt) {
        return jpaAttemptRepository.save(attempt);
    }

    @Override
    public Optional<Attempt> findAttemptById(
            String attemptId) {

        return jpaAttemptRepository
                .findByAttemptId(attemptId);
    }

    @Override
    public Optional<Attempt> findAttemptByIdForUpdate(String attemptId) {
        return jpaAttemptRepository.findByAttemptIdForUpdate(attemptId);
    }

    @Override
    public List<Attempt> findAttemptsByUserId(
            String userId) {

        return jpaAttemptRepository
                .findByUserId(userId);
    }

    // Response
    @Override
    public Response save(Response response) {
        return jpaResponseRepository.save(response);
    }

    @Override
    public List<Response> saveResponses(
            java.util.Collection<Response> responses) {
        if (responses == null || responses.isEmpty()) {
            return List.of();
        }
        return jpaResponseRepository.saveAll(responses);
    }


    @Override
    public List<Response> findResponsesByAttemptId(String attemptId) {
        return jpaResponseRepository.findByAttemptId(attemptId);
    }

    @Override
    public List<Response> findResponsesByAttemptIds(java.util.Collection<String> attemptIds) {
        return attemptIds == null || attemptIds.isEmpty()
                ? List.of() : jpaResponseRepository.findByAttemptIdIn(attemptIds);
    }

    @Override
    public void deleteResponsesByQuestionId(String questionId) {
        jpaResponseRepository.deleteByQuestionId(questionId);
    }

    // QuizQuestion
    @Override
    public void deleteQuizQuestionsByQuizId(
            String quizId) {

        jpaQuizQuestionRepository
                .deleteByQuizId(quizId);
    }

    // =========================================================================
    // Phase 2 (P0)
    // =========================================================================

    @Override
    public List<Question> findQuestionsByQuizId(String quizId) {
        return jpaQuestionRepository.findByQuizIdOrderByQuizIndex(quizId);
    }

    // =========================================================================
    // Phase 2 (P1): Skill mapping
    // =========================================================================

    @Override
    public QuestionSkill saveQuestionSkill(QuestionSkill questionSkill) {
        return jpaQuestionSkillRepository.save(questionSkill);
    }

    @Override
    public List<QuestionSkill> findQuestionSkillsByQuestionId(String questionId) {
        return jpaQuestionSkillRepository.findByQuestionId(questionId);
    }

    @Override
    public List<QuestionSkill> findQuestionSkillsByQuestionIds(java.util.Collection<String> questionIds) {
        return questionIds == null || questionIds.isEmpty()
                ? List.of() : jpaQuestionSkillRepository.findByQuestionIdIn(questionIds);
    }

    @Override
    public void deleteQuestionSkillsByQuestionId(String questionId) {
        jpaQuestionSkillRepository.deleteByQuestionId(questionId);
    }

    @Override
    public void reassignQuestionsBySubjectId(String subjectId, String targetSubjectId, String targetChapterId) {
        jpaQuestionRepository.reassignQuestionsBySubjectId(subjectId, targetSubjectId, targetChapterId);
    }

    @Override
    public void reassignQuestionsByChapterId(String chapterId, String targetSubjectId, String targetChapterId) {
        jpaQuestionRepository.reassignQuestionsByChapterId(chapterId, targetSubjectId, targetChapterId);
    }

    @Override
    public void reassignQuestionsByChapterIds(java.util.Collection<String> chapterIds, String targetSubjectId, String targetChapterId) {
        if (chapterIds == null || chapterIds.isEmpty()) return;
        jpaQuestionRepository.reassignQuestionsByChapterIds(chapterIds, targetSubjectId, targetChapterId);
    }

    @Override
    public void detachQuestionsBySubjectId(String subjectId) {
        jpaQuestionRepository.detachQuestionsBySubjectId(subjectId);
    }

    @Override
    public void detachQuestionsByChapterId(String chapterId) {
        jpaQuestionRepository.detachQuestionsByChapterId(chapterId);
    }

    @Override
    public void detachQuestionsByChapterIds(java.util.Collection<String> chapterIds) {
        if (chapterIds == null || chapterIds.isEmpty()) return;
        jpaQuestionRepository.detachQuestionsByChapterIds(chapterIds);
    }
}

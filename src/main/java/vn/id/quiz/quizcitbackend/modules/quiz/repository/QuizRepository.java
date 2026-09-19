package vn.id.quiz.quizcitbackend.modules.quiz.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.QuestionSkill;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Attempt;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Option;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Question;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Quiz;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizQuestion;
import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.Response;

public interface QuizRepository {

    // =========================================================================
    // Question
    // =========================================================================

    Question save(
            Question question);

    Optional<Question> findQuestionById(
            String questionId);

    List<Question> findQuestionsByChapterId(
            String chapterId);
    default List<Question> findQuestionsByChapterIds(Collection<String> chapterIds) {
        return chapterIds == null ? List.of() : chapterIds.stream()
                .flatMap(id -> findQuestionsByChapterId(id).stream()).toList();
    }
    
    default List<Question> findAnswerableQuestionsByChapterIds(Collection<String> chapterIds) {
        return findQuestionsByChapterIds(chapterIds);
    }
    
    default List<Question> findAllQuestions() { return List.of(); }

    default Map<String, Long> countQuestionsByChapterIds(Collection<String> chapterIds) { return Map.of(); }

    default List<Question> findQuestionsBySubjectId(String subjectId) { return List.of(); }

    default List<Question> findQuestionsBySubjectIdAndChapterId(String subjectId, String chapterId) { return List.of(); }

    default Page<Question> findQuestionsPaged(String subjectId, String chapterId, Pageable pageable) {
        return Page.empty(pageable);
    }

    // Batch: lấy nhiều câu hỏi theo danh sách id (tránh N+1).
    List<Question> findQuestionsByIds(
            Collection<String> questionIds);


    void deleteQuestionById(
            String questionId);

    default void softDeleteQuestionById(String questionId) {
        deleteQuestionById(questionId);
    }

    default void hardDeleteQuestionById(String questionId) {
        deleteQuestionById(questionId);
    }

    // =========================================================================
    // Quiz
    // =========================================================================

    Quiz save(
            Quiz quiz);

    Optional<Quiz> findQuizById(
            String quizId);

    List<Quiz> findQuizzesByChapterId(
            String chapterId);
    default int nextQuizDisplayOrder(String chapterId) { return 1; }

    /**
     * Phase 2 (P1): lấy tất cả quiz thuộc về một subject.
     */
    List<Quiz> findQuizzesBySubjectId(
            String subjectId);
    default List<Quiz> findNonAdaptiveQuizzesBySubjectId(String subjectId) {
        return findQuizzesBySubjectId(subjectId).stream()
                .filter(quiz -> quiz.getType() != vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.QuizType.ADAPTIVE)
                .toList();
    }
    default List<Quiz> findQuizzesByIds(Collection<String> quizIds) {
        return quizIds == null ? List.of() : quizIds.stream().map(this::findQuizById)
                .flatMap(Optional::stream).toList();
    }

    void deleteQuizById(
            String quizId);

    default void softDeleteQuizById(String quizId) {
        deleteQuizById(quizId);
    }

    default void hardDeleteQuizById(String quizId) {
        deleteQuizById(quizId);
    }

    default boolean existsAttemptByQuizId(String quizId) {
        return false;
    }

    // =========================================================================
    // Quiz Question
    // =========================================================================

    QuizQuestion save(
            QuizQuestion quizQuestion);

    List<QuizQuestion> findQuizQuestionsByQuizId(
            String quizId);

    void deleteQuizQuestionsByQuestionId(
            String questionId);

    void deleteQuizQuestionsByQuizId(
            String quizId);

    // =========================================================================
    // Option
    // =========================================================================

    Option save(
            Option option);

    Optional<Option> findOptionById(
            String optionId);

    List<Option> findOptionsByQuestionId(
            String questionId);

    // Batch: lấy option của nhiều câu hỏi (tránh N+1).
    List<Option> findOptionsByQuestionIds(
            Collection<String> questionIds);


    void deleteOptionsByQuestionId(
            String questionId);

    // =========================================================================
    // Attempt
    // =========================================================================

    Attempt save(
            Attempt attempt);

    Optional<Attempt> findAttemptById(
            String attemptId);

    default Optional<Attempt> findAttemptByIdForUpdate(String attemptId) {
        return findAttemptById(attemptId);
    }

    List<Attempt> findAttemptsByUserId(
            String userId);

    // =========================================================================
    // Response
    // =========================================================================

    Response save(
            Response response);

    // Batch: lưu nhiều response trong 1 lần (tránh N insert riêng lẻ).
    List<Response> saveResponses(
            Collection<Response> responses);

    List<Response> findResponsesByAttemptId(
            String attemptId);
    default List<Response> findResponsesByAttemptIds(Collection<String> attemptIds) {
        return attemptIds == null ? List.of() : attemptIds.stream()
                .flatMap(id -> findResponsesByAttemptId(id).stream()).toList();
    }
    
    default Map<String, Long> countResponsesByAttemptIds(Collection<String> attemptIds) {
        return Map.of();
    }

    void deleteResponsesByQuestionId(
            String questionId);

    // =========================================================================
    // Phase 2 (P0): API lấy câu hỏi trong 1 quiz kèm option
    // =========================================================================

    /**
     * Trả về danh sách câu hỏi của một quiz theo thứ tự index trong quiz.
     */
    List<Question> findQuestionsByQuizId(String quizId);

    // =========================================================================
    // Phase 2 (P1): Skill mapping cho câu hỏi
    // =========================================================================

    QuestionSkill saveQuestionSkill(QuestionSkill questionSkill);

    List<QuestionSkill> findQuestionSkillsByQuestionId(String questionId);

    default List<QuestionSkill> findQuestionSkillsByQuestionIds(Collection<String> questionIds) {
        return questionIds == null ? List.of() : questionIds.stream()
                .flatMap(id -> findQuestionSkillsByQuestionId(id).stream()).toList();
    }

    void deleteQuestionSkillsByQuestionId(String questionId);

    default void reassignQuestionsBySubjectId(String subjectId, String targetSubjectId, String targetChapterId) {}

    default void reassignQuestionsByChapterId(String chapterId, String targetSubjectId, String targetChapterId) {}

    default void reassignQuestionsByChapterIds(Collection<String> chapterIds, String targetSubjectId, String targetChapterId) {}

    default void detachQuestionsBySubjectId(String subjectId) {}

    default void detachQuestionsByChapterId(String chapterId) {}

    default void detachQuestionsByChapterIds(Collection<String> chapterIds) {}
}

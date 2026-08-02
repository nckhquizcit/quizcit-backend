package vn.id.quiz.quizcitbackend.modules.quiz.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


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

    // Batch: lấy nhiều câu hỏi theo danh sách id (tránh N+1).
    List<Question> findQuestionsByIds(
            Collection<String> questionIds);


    void deleteQuestionById(
            String questionId);

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

    void deleteQuizById(
            String quizId);

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

    void deleteQuestionSkillsByQuestionId(String questionId);
}

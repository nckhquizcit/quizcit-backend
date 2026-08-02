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

        jpaQuestionRepository
                .deleteByQuestionId(questionId);
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
    public void deleteQuizById(
            String quizId) {

        jpaQuizRepository
                .deleteByQuizId(quizId);
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
        // Lấy quan hệ quiz-question đã sắp xếp theo index, rồi map sang Question
        List<QuizQuestion> quizQuestions =
                jpaQuizQuestionRepository.findByQuizIdOrderByIndexOrder(quizId);
        return quizQuestions.stream()
                .map(qq -> jpaQuestionRepository
                        .findByQuestionId(qq.getQuestionId())
                        .orElse(null))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
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
    public void deleteQuestionSkillsByQuestionId(String questionId) {
        jpaQuestionSkillRepository.deleteByQuestionId(questionId);
    }
}

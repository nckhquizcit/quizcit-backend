package vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence;

public interface AttemptResponseCountProjection {
    String getAttemptId();
    long getCount();
}

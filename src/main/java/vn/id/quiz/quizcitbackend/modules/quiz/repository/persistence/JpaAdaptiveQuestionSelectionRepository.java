package vn.id.quiz.quizcitbackend.modules.quiz.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.id.quiz.quizcitbackend.modules.quiz.presentation.model.AdaptiveQuestionSelection;

public interface JpaAdaptiveQuestionSelectionRepository
        extends JpaRepository<AdaptiveQuestionSelection, String> {
    void deleteByUserId(String userId);
}

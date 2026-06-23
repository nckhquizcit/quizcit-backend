package vn.id.quiz.quizcitbackend.modules.quiz.presentation.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class QuizQuestionId implements Serializable {
    private String quizId;
    private String questionId;
}
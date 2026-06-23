package vn.id.quiz.quizcitbackend.modules.progress.presentation.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class QuestionSkillId implements Serializable {
    private String questionId;
    private String skillId;
}
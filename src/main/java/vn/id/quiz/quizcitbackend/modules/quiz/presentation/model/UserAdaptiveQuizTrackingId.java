package vn.id.quiz.quizcitbackend.modules.quiz.presentation.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserAdaptiveQuizTrackingId implements Serializable {
    private String userId;
    private String subjectId;
}

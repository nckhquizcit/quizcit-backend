package vn.id.quiz.quizcitbackend.modules.content.presentation.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ChapterSkillId implements Serializable {
    private String chapterId;
    private String skillId;
}

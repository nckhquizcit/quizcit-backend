package vn.id.quiz.quizcitbackend.modules.content.presentation.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UserUnlockedChapterId implements Serializable {
    private String userId;
    private String chapterId;
}

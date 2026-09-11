package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TheoryStudyOverviewResponse {
    private String theoryId;
    private String chapterId;
    private String title;
    private int displayOrder;
}

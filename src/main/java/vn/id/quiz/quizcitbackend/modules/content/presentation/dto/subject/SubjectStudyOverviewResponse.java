package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectStudyOverviewResponse {
    private String subjectId;
    private String name;
    private String courseCode;
    private String imageUrl;
    private String description;
    private boolean enrolled;
    private ZonedDateTime enrolledAt;
    private List<ChapterStudyOverviewResponse> chapters;
}

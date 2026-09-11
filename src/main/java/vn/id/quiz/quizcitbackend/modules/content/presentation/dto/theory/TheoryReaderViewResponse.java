package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.theory;

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
public class TheoryReaderViewResponse {
    private String theoryId;
    private String chapterId;
    private String subjectId;
    private String subjectName;
    private String title;
    private String content;
    private String contentFormat;
    private ZonedDateTime createdAt;
    private ChapterInfo chapter;
    private int chapterIndex;
    private List<TheorySummaryItem> chapterTheories;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChapterInfo {
        private String chapterId;
        private String name;
        private int displayOrder;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TheorySummaryItem {
        private String theoryId;
        private String title;
        private int displayOrder;
    }
}

package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter;

import java.time.ZonedDateTime;
import java.util.List;

public record ChapterResponse(
        String chapterId,
        String subjectId,
        String name,
        ZonedDateTime createdAt,
        int displayOrder,
        List<ChapterSkillResponse> skills,
        boolean unlocked
) {
    public ChapterResponse{
        if(createdAt==null){
            createdAt=ZonedDateTime.now();
        }
    }

    public ChapterResponse(
            String chapterId,
            String subjectId,
            String name,
            ZonedDateTime createdAt,
            int displayOrder,
            List<ChapterSkillResponse> skills
    ) {
        this(chapterId, subjectId, name, createdAt, displayOrder, skills, true);
    }
}

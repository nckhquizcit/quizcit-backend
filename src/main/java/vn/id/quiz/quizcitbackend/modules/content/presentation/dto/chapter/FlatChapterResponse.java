package vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter;

public record FlatChapterResponse(
        String chapterId,
        String chapterName,
        String name,
        String subjectId,
        String subjectName,
        int displayOrder
) {
    public FlatChapterResponse(
            String chapterId,
            String chapterName,
            String subjectId,
            String subjectName,
            int displayOrder
    ) {
        this(chapterId, chapterName, chapterName, subjectId, subjectName, displayOrder);
    }
}

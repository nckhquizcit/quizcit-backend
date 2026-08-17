package vn.id.quiz.quizcitbackend.modules.content.presentation.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChapterTest {

    @Test
    void createShouldSetDefaults() {
        Chapter chapter = Chapter.create("subject-1", "Chương 1", 1);

        assertThat(chapter.getChapterId()).isNotBlank();
        assertThat(chapter.getSubjectId()).isEqualTo("subject-1");
        assertThat(chapter.getName()).isEqualTo("Chương 1");
        assertThat(chapter.getCreatedAt()).isNotNull();
        assertThat(chapter.getDeletedAt()).isNull();
    }

    @Test
    void softDeleteShouldSetDeletedAt() {
        Chapter chapter = Chapter.create("subject-1", "Chương 1", 1);

        chapter.softDelete();

        assertThat(chapter.getDeletedAt()).isNotNull();
    }
}

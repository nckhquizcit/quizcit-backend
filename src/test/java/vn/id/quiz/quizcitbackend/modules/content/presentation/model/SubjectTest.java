package vn.id.quiz.quizcitbackend.modules.content.presentation.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SubjectTest {

    @Test
    void createShouldSetDefaults() {
        Subject subject = Subject.create("CT101", "Lập trình C", "Mô tả", 1);

        assertThat(subject.getSubjectId()).isNotBlank();
        assertThat(subject.getCourseCode()).isEqualTo("CT101");
        assertThat(subject.getName()).isEqualTo("Lập trình C");
        assertThat(subject.isVisible()).isTrue();
        assertThat(subject.getCreatedAt()).isNotNull();
        assertThat(subject.getDeletedAt()).isNull();
    }

    @Test
    void createWithImageUrlShouldSetImageUrl() {
        Subject subject = Subject.create("CT101", "Lập trình C", "Mô tả", 1, "https://example.com/c.png");

        assertThat(subject.getImageUrl()).isEqualTo("https://example.com/c.png");
    }

    @Test
    void updateSubjectShouldUpdateImageUrl() {
        Subject subject = Subject.create("CT101", "Lập trình C", "Mô tả", 1);
        subject.updateSubject("CT101", "Lập trình C nâng cao", "Mô tả mới", true, 2, "https://example.com/new.png");

        assertThat(subject.getName()).isEqualTo("Lập trình C nâng cao");
        assertThat(subject.getImageUrl()).isEqualTo("https://example.com/new.png");
    }

    @Test
    void softDeleteShouldSetDeletedAtAndHideSubject() {
        Subject subject = Subject.create("CT101", "Lập trình C", "Mô tả", 1);

        subject.softDelete();

        assertThat(subject.getDeletedAt()).isNotNull();
        assertThat(subject.isVisible()).isFalse();
    }
}

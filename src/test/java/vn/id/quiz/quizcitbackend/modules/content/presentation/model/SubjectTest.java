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
    void softDeleteShouldSetDeletedAtAndHideSubject() {
        Subject subject = Subject.create("CT101", "Lập trình C", "Mô tả", 1);

        subject.softDelete();

        assertThat(subject.getDeletedAt()).isNotNull();
        assertThat(subject.isVisible()).isFalse();
    }
}

package vn.id.quiz.quizcitbackend.modules.content.presentation.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TheoryTest {

    @Test
    void createShouldSetDefaults() {
        Theory theory = Theory.create("chapter-1", "Nội dung lý thuyết", "plain");

        assertThat(theory.getTheoryId()).isNotBlank();
        assertThat(theory.getChapterId()).isEqualTo("chapter-1");
        assertThat(theory.getContent()).isEqualTo("Nội dung lý thuyết");
        assertThat(theory.getContentFormat()).isEqualTo("plain");
        assertThat(theory.getCreatedAt()).isNotNull();
        assertThat(theory.getDeletedAt()).isNull();
    }

    @Test
    void softDeleteShouldSetDeletedAt() {
        Theory theory = Theory.create("chapter-1", "Nội dung lý thuyết");

        theory.softDelete();

        assertThat(theory.getDeletedAt()).isNotNull();
    }
}

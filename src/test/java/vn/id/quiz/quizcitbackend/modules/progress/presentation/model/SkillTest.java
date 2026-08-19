package vn.id.quiz.quizcitbackend.modules.progress.presentation.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SkillTest {

    @Test
    void createSetsInitialFieldsAndActiveStatus() {
        Skill skill = Skill.create("Đệ quy", "Mô tả đệ quy");

        assertThat(skill.getSkillId()).isNotBlank();
        assertThat(skill.getName()).isEqualTo("Đệ quy");
        assertThat(skill.getDescription()).isEqualTo("Mô tả đệ quy");
        assertThat(skill.getCreatedAt()).isNotNull();
        assertThat(skill.getDeletedAt()).isNull();
        assertThat(skill.isDeleted()).isFalse();
    }

    @Test
    void updateSkillUpdatesNameAndDescription() {
        Skill skill = Skill.create("Cũ", "Mô tả cũ");
        skill.updateSkill("Mới", "Mô tả mới");

        assertThat(skill.getName()).isEqualTo("Mới");
        assertThat(skill.getDescription()).isEqualTo("Mô tả mới");
    }

    @Test
    void softDeleteMarksSkillAsDeleted() {
        Skill skill = Skill.create("Kỹ năng", "Mô tả");
        assertThat(skill.isDeleted()).isFalse();

        skill.softDelete();

        assertThat(skill.isDeleted()).isTrue();
        assertThat(skill.getDeletedAt()).isNotNull();
    }
}

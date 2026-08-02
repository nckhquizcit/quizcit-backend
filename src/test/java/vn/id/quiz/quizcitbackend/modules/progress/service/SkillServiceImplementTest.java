package vn.id.quiz.quizcitbackend.modules.progress.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaChapterSkillRepository;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.Skill;
import vn.id.quiz.quizcitbackend.modules.progress.repository.SkillRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.persistence.JpaAbilityHistoryRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.persistence.JpaQuestionSkillRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.persistence.JpaUserSkillAbilityRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

class SkillServiceImplementTest {
    private SkillRepository skillRepository;
    private JpaChapterSkillRepository chapterSkillRepository;
    private JpaQuestionSkillRepository questionSkillRepository;
    private JpaUserSkillAbilityRepository userSkillAbilityRepository;
    private JpaAbilityHistoryRepository abilityHistoryRepository;
    private SkillServiceImplement service;

    @BeforeEach
    void setUp() {
        skillRepository = mock(SkillRepository.class);
        chapterSkillRepository = mock(JpaChapterSkillRepository.class);
        questionSkillRepository = mock(JpaQuestionSkillRepository.class);
        userSkillAbilityRepository = mock(JpaUserSkillAbilityRepository.class);
        abilityHistoryRepository = mock(JpaAbilityHistoryRepository.class);
        service = new SkillServiceImplement(skillRepository, chapterSkillRepository,
                questionSkillRepository, userSkillAbilityRepository, abilityHistoryRepository);
        when(skillRepository.findById("skill-1"))
                .thenReturn(Optional.of(Skill.builder().skillId("skill-1").name("Vòng lặp").build()));
    }

    @Test
    void deleteSkillRejectsSkillUsedByQuestion() {
        when(questionSkillRepository.existsBySkillId("skill-1")).thenReturn(true);

        assertDeletionRejected();
    }

    @Test
    void deleteSkillRejectsSkillUsedByChapter() {
        when(chapterSkillRepository.existsBySkillId("skill-1")).thenReturn(true);

        assertDeletionRejected();
    }

    @Test
    void deleteSkillRejectsSkillUsedByAbility() {
        when(userSkillAbilityRepository.existsBySkillId("skill-1")).thenReturn(true);

        assertDeletionRejected();
    }

    @Test
    void deleteSkillRejectsSkillUsedByHistory() {
        when(abilityHistoryRepository.existsBySkillId("skill-1")).thenReturn(true);

        assertDeletionRejected();
    }

    private void assertDeletionRejected() {

        assertThatThrownBy(() -> service.deleteSkill("skill-1"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("đang được sử dụng");
        verify(skillRepository, never()).deleteById("skill-1");
    }

    @Test
    void deleteSkillDeletesUnusedSkill() {
        service.deleteSkill("skill-1");

        verify(skillRepository).deleteById("skill-1");
    }
}

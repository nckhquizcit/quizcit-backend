package vn.id.quiz.quizcitbackend.modules.progress.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.SkillRequest;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.SkillResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.Skill;
import vn.id.quiz.quizcitbackend.modules.progress.repository.SkillRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaChapterSkillRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.persistence.JpaAbilityHistoryRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.persistence.JpaQuestionSkillRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.persistence.JpaUserSkillAbilityRepository;

class SkillServiceImplementTest {
    private SkillRepository skillRepository;
    private JpaQuestionSkillRepository questionSkillRepository;
    private JpaChapterSkillRepository chapterSkillRepository;
    private JpaUserSkillAbilityRepository userSkillAbilityRepository;
    private JpaAbilityHistoryRepository abilityHistoryRepository;
    private SkillServiceImplement service;

    @BeforeEach
    void setUp() {
        skillRepository = mock(SkillRepository.class);
        questionSkillRepository = mock(JpaQuestionSkillRepository.class);
        chapterSkillRepository = mock(JpaChapterSkillRepository.class);
        userSkillAbilityRepository = mock(JpaUserSkillAbilityRepository.class);
        abilityHistoryRepository = mock(JpaAbilityHistoryRepository.class);
        service = new SkillServiceImplement(
                skillRepository,
                questionSkillRepository,
                chapterSkillRepository,
                userSkillAbilityRepository,
                abilityHistoryRepository);
    }

    @Test
    void deleteSkillSoftDeletesSkill() {
        Skill skill = Skill.builder().skillId("skill-1").name("Vòng lặp").build();
        when(skillRepository.findById("skill-1")).thenReturn(Optional.of(skill));

        service.deleteSkill("skill-1");

        assertThat(skill.isDeleted()).isTrue();
        assertThat(skill.getDeletedAt()).isNotNull();
        verify(skillRepository).save(skill);
    }

    @Test
    void hardDeleteSkillThrowsExceptionWhenHardDeleteAttempted() {
        assertThatThrownBy(() -> service.hardDeleteSkill("skill-1"))
                .isInstanceOf(AppException.class)
                .hasMessage("Hệ thống chỉ hỗ trợ xóa mềm, không cho phép xóa cứng dữ liệu.");
    }

    @Test
    void deleteSkillThrowsNotFoundWhenSkillMissing() {
        when(skillRepository.findById("missing-skill")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteSkill("missing-skill"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Không tìm thấy kỹ năng");
    }

    @Test
    void getAllSkillsReturnsMappedList() {
        Skill skill1 = Skill.builder().skillId("s1").name("Skill 1").description("Desc 1").build();
        Skill skill2 = Skill.builder().skillId("s2").name("Skill 2").description("Desc 2").build();
        when(skillRepository.findAll()).thenReturn(List.of(skill1, skill2));

        List<SkillResponse> responses = service.getAllSkills();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getSkillId()).isEqualTo("s1");
        assertThat(responses.get(1).getName()).isEqualTo("Skill 2");
    }

    @Test
    void getSkillByIdReturnsSkill() {
        Skill skill = Skill.builder().skillId("s1").name("Skill 1").description("Desc 1").build();
        when(skillRepository.findById("s1")).thenReturn(Optional.of(skill));

        SkillResponse response = service.getSkillById("s1");

        assertThat(response.getSkillId()).isEqualTo("s1");
        assertThat(response.getName()).isEqualTo("Skill 1");
    }

    @Test
    void createSkillSavesAndReturnsSkill() {
        when(skillRepository.save(any(Skill.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SkillRequest request = SkillRequest.builder()
                .name("New Skill")
                .description("New Desc")
                .build();

        SkillResponse response = service.createSkill(request);

        assertThat(response.getName()).isEqualTo("New Skill");
        assertThat(response.getDescription()).isEqualTo("New Desc");
        assertThat(response.getSkillId()).isNotBlank();
    }

    @Test
    void updateSkillUpdatesAndSaves() {
        Skill skill = Skill.builder().skillId("s1").name("Old Name").description("Old Desc").build();
        when(skillRepository.findById("s1")).thenReturn(Optional.of(skill));
        when(skillRepository.save(any(Skill.class))).thenAnswer(inv -> inv.getArgument(0));

        SkillRequest request = SkillRequest.builder()
                .name("Updated Name")
                .description("Updated Desc")
                .build();

        SkillResponse response = service.updateSkill("s1", request);

        assertThat(response.getName()).isEqualTo("Updated Name");
        assertThat(response.getDescription()).isEqualTo("Updated Desc");
        assertThat(skill.getName()).isEqualTo("Updated Name");
    }
}

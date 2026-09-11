package vn.id.quiz.quizcitbackend.modules.progress.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.AbilityHistoryResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.UserProgressOverviewResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.UserProgressSkillResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.UserSkillDetailOverviewResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.Skill;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.UserSkillAbilityResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.AbilityHistory;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.UserSkillAbility;
import vn.id.quiz.quizcitbackend.modules.progress.repository.ProgressRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.SkillRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

class ProgressServiceImplementTest {
    private ProgressRepository progressRepository;
    private SkillRepository skillRepository;
    private ProgressServiceImplement service;

    @BeforeEach
    void setUp() {
        progressRepository = mock(ProgressRepository.class);
        skillRepository = mock(SkillRepository.class);
        service = new ProgressServiceImplement(progressRepository, skillRepository);
    }

    @Test
    void getUserSkillAbilityReturnsMappedResponse() {
        UserSkillAbility ability = UserSkillAbility.builder()
                .id("usa-1")
                .userId("u1")
                .skillId("s1")
                .theta(2.5)
                .updatedAt(ZonedDateTime.now())
                .build();
        when(progressRepository.findUserSkillAbility("u1", "s1")).thenReturn(Optional.of(ability));
        when(skillRepository.findSkillNameById("s1")).thenReturn(Optional.of("Vòng lặp"));

        UserSkillAbilityResponse response = service.getUserSkillAbility("u1", "s1");

        assertThat(response.id()).isEqualTo("usa-1");
        assertThat(response.userId()).isEqualTo("u1");
        assertThat(response.skillId()).isEqualTo("s1");
        assertThat(response.skillName()).isEqualTo("Vòng lặp");
        assertThat(response.theta()).isEqualTo(2.5);
    }

    @Test
    void getUserSkillAbilityThrowsNotFoundWhenMissing() {
        when(progressRepository.findUserSkillAbility("u1", "s1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserSkillAbility("u1", "s1"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Không tìm thấy dữ liệu năng lực");
    }

    @Test
    void getUserSkillAbilitiesBatchLoadsSkillNames() {
        UserSkillAbility ability1 = UserSkillAbility.builder().id("usa-1").userId("u1").skillId("s1").theta(1.0).build();
        UserSkillAbility ability2 = UserSkillAbility.builder().id("usa-2").userId("u1").skillId("s2").theta(2.0).build();

        when(progressRepository.findUserSkillAbilities("u1")).thenReturn(List.of(ability1, ability2));
        when(skillRepository.findSkillNamesByIds(Set.of("s1", "s2"))).thenReturn(Map.of("s1", "Kỹ năng 1", "s2", "Kỹ năng 2"));

        List<UserSkillAbilityResponse> responses = service.getUserSkillAbilities("u1");

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).skillName()).isEqualTo("Kỹ năng 1");
        assertThat(responses.get(1).skillName()).isEqualTo("Kỹ năng 2");
    }

    @Test
    void getAbilityHistoriesBatchLoadsSkillNames() {
        AbilityHistory h1 = AbilityHistory.builder().historyId("h1").userId("u1").skillId("s1").theta(1.5).delta(0.5).createdAt(LocalDateTime.now()).build();
        AbilityHistory h2 = AbilityHistory.builder().historyId("h2").userId("u1").skillId("s2").theta(2.5).delta(0.2).createdAt(LocalDateTime.now()).build();

        when(progressRepository.findAbilityHistoriesByUserId("u1")).thenReturn(List.of(h1, h2));
        when(skillRepository.findSkillNamesByIds(Set.of("s1", "s2"))).thenReturn(Map.of("s1", "Kỹ năng 1", "s2", "Kỹ năng 2"));

        List<AbilityHistoryResponse> responses = service.getAbilityHistories("u1");

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).skillName()).isEqualTo("Kỹ năng 1");
        assertThat(responses.get(1).skillName()).isEqualTo("Kỹ năng 2");
    }

    @Test
    void getAbilityHistoriesBySkillResolvesSingleSkillName() {
        AbilityHistory h1 = AbilityHistory.builder().historyId("h1").userId("u1").skillId("s1").theta(1.5).delta(0.5).createdAt(LocalDateTime.now()).build();

        when(progressRepository.findAbilityHistoriesByUserIdAndSkillId("u1", "s1")).thenReturn(List.of(h1));
        when(skillRepository.findSkillNameById("s1")).thenReturn(Optional.of("Kỹ năng 1"));

        List<AbilityHistoryResponse> responses = service.getAbilityHistoriesBySkill("u1", "s1");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).skillName()).isEqualTo("Kỹ năng 1");
    }

    @Test
    void getAverageAbilityReturnsRepositoryValue() {
        when(progressRepository.getAverageAbility("u1")).thenReturn(2.35);

        double avg = service.getAverageAbility("u1");

        assertThat(avg).isEqualTo(2.35);
    }
    @Test
    void getUserProgressOverviewReturnsAggregatedData() {
        UserSkillAbility a1 = UserSkillAbility.builder().id("usa-1").userId("u1").skillId("s1").theta(1.0).build();
        UserSkillAbility a2 = UserSkillAbility.builder().id("usa-2").userId("u1").skillId("s2").theta(2.0).build();
        Skill s1 = Skill.builder().skillId("s1").name("Vòng lặp").description("Mô tả 1").build();
        Skill s2 = Skill.builder().skillId("s2").name("Hàm").description("Mô tả 2").build();
        AbilityHistory h1 = AbilityHistory.builder().historyId("h1").userId("u1").skillId("s1").theta(1.0).delta(0.2).createdAt(LocalDateTime.now()).build();

        when(progressRepository.findUserSkillAbilities("u1")).thenReturn(List.of(a1, a2));
        when(progressRepository.getAverageAbility("u1")).thenReturn(1.5);
        when(skillRepository.findAll()).thenReturn(List.of(s1, s2));
        when(progressRepository.findAbilityHistoriesByUserId("u1")).thenReturn(List.of(h1));

        UserProgressOverviewResponse overview = service.getUserProgressOverview("u1");

        assertThat(overview.averageTheta()).isEqualTo(1.5);
        assertThat(overview.trackedSkillCount()).isEqualTo(2);
        assertThat(overview.totalHistoryCount()).isEqualTo(1);
        assertThat(overview.skills()).hasSize(2);
        assertThat(overview.skills().get(0).name()).isEqualTo("Vòng lặp");
        assertThat(overview.skills().get(0).description()).isEqualTo("Mô tả 1");
        assertThat(overview.recentHistories()).hasSize(1);
        assertThat(overview.recentHistories().get(0).skillName()).isEqualTo("Vòng lặp");
    }

    @Test
    void getUserSkillsSummaryReturnsAllSkillsWithMetadata() {
        UserSkillAbility a1 = UserSkillAbility.builder().id("usa-1").userId("u1").skillId("s1").theta(1.0).build();
        Skill s1 = Skill.builder().skillId("s1").name("Vòng lặp").description("Mô tả 1").build();

        when(progressRepository.findUserSkillAbilities("u1")).thenReturn(List.of(a1));
        when(skillRepository.findAll()).thenReturn(List.of(s1));

        List<UserProgressSkillResponse> result = service.getUserSkillsSummary("u1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).skillId()).isEqualTo("s1");
        assertThat(result.get(0).name()).isEqualTo("Vòng lặp");
        assertThat(result.get(0).description()).isEqualTo("Mô tả 1");
    }

    @Test
    void getUserSkillDetailOverviewReturnsSkillAndOtherSkills() {
        Skill targetSkill = Skill.builder().skillId("s1").name("Vòng lặp").description("Mô tả 1").build();
        Skill otherSkill = Skill.builder().skillId("s2").name("Hàm").description("Mô tả 2").build();
        UserSkillAbility a1 = UserSkillAbility.builder().id("usa-1").userId("u1").skillId("s1").theta(2.5).updatedAt(ZonedDateTime.now()).build();
        UserSkillAbility a2 = UserSkillAbility.builder().id("usa-2").userId("u1").skillId("s2").theta(1.5).updatedAt(ZonedDateTime.now()).build();
        AbilityHistory h1 = AbilityHistory.builder().historyId("h1").userId("u1").skillId("s1").theta(2.5).delta(0.5).createdAt(LocalDateTime.now()).build();

        when(skillRepository.findById("s1")).thenReturn(Optional.of(targetSkill));
        when(progressRepository.findUserSkillAbility("u1", "s1")).thenReturn(Optional.of(a1));
        when(progressRepository.findAbilityHistoriesByUserIdAndSkillId("u1", "s1")).thenReturn(List.of(h1));
        when(progressRepository.findUserSkillAbilities("u1")).thenReturn(List.of(a1, a2));
        when(skillRepository.findAll()).thenReturn(List.of(targetSkill, otherSkill));

        UserSkillDetailOverviewResponse detail = service.getUserSkillDetailOverview("u1", "s1");

        assertThat(detail.skillId()).isEqualTo("s1");
        assertThat(detail.name()).isEqualTo("Vòng lặp");
        assertThat(detail.currentTheta()).isEqualTo(2.5);
        assertThat(detail.histories()).hasSize(1);
        assertThat(detail.histories().get(0).skillName()).isEqualTo("Vòng lặp");
        assertThat(detail.otherSkills()).hasSize(1);
        assertThat(detail.otherSkills().get(0).skillId()).isEqualTo("s2");
        assertThat(detail.otherSkills().get(0).name()).isEqualTo("Hàm");
    }
}

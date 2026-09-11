package vn.id.quiz.quizcitbackend.modules.progress.service;

import java.util.List;

import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.AbilityHistoryResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.UserProgressOverviewResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.UserProgressSkillResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.UserSkillDetailOverviewResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.UserSkillAbilityResponse;

public interface ProgressService {

    UserSkillAbilityResponse getUserSkillAbility(
            String userId,
            String skillId);

    List<UserSkillAbilityResponse> getUserSkillAbilities(
            String userId);

    List<AbilityHistoryResponse> getAbilityHistories(
            String userId);

    List<AbilityHistoryResponse> getAbilityHistoriesBySkill(
            String userId,
            String skillId);
    
    double getAverageAbility(String userId);

    /**
     * Phase 4: Tổng quan tiến độ học tập của người dùng (averageTheta, trackedSkillCount, skills kèm metadata, 12 lịch sử gần nhất).
     */
    UserProgressOverviewResponse getUserProgressOverview(String userId);

    /**
     * Phase 4: Danh sách tóm tắt tất cả kỹ năng người dùng đang theo dõi kèm tên, mô tả và theta hiện tại.
     */
    List<UserProgressSkillResponse> getUserSkillsSummary(String userId);

    /**
     * Phase 4: Chi tiết tiến độ một kỹ năng cụ thể kèm biến động theta và danh sách các kỹ năng khác để điều hướng.
     */
    UserSkillDetailOverviewResponse getUserSkillDetailOverview(String userId, String skillId);


    
}
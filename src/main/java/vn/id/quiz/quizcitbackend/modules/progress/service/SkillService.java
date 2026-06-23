package vn.id.quiz.quizcitbackend.modules.progress.service;

import java.util.List;

import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.SkillRequest;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.SkillResponse;

/**
 * Use case cho Skill - Phase 2 (P1).
 */
public interface SkillService {
    List<SkillResponse> getAllSkills();
    SkillResponse getSkillById(String skillId);
    SkillResponse createSkill(SkillRequest request);
    SkillResponse updateSkill(String skillId, SkillRequest request);
    void deleteSkill(String skillId);
}

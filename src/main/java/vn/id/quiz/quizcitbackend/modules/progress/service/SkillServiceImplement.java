package vn.id.quiz.quizcitbackend.modules.progress.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.SkillRequest;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.dto.SkillResponse;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.Skill;
import vn.id.quiz.quizcitbackend.modules.progress.repository.SkillRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaChapterSkillRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.persistence.JpaAbilityHistoryRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.persistence.JpaQuestionSkillRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.persistence.JpaUserSkillAbilityRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import vn.id.quiz.quizcitbackend.shared.config.CacheConfig;

@Service
@RequiredArgsConstructor
public class SkillServiceImplement implements SkillService {

    private final SkillRepository skillRepository;
    private final JpaQuestionSkillRepository questionSkillRepository;
    private final JpaChapterSkillRepository chapterSkillRepository;
    private final JpaUserSkillAbilityRepository userSkillAbilityRepository;
    private final JpaAbilityHistoryRepository abilityHistoryRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_SKILLS, key = "'all'")
    public List<SkillResponse> getAllSkills() {
        return skillRepository.findAll().stream()
                .map(SkillResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_SKILL_DETAIL, key = "#skillId")
    public SkillResponse getSkillById(String skillId) {
        return skillRepository.findById(skillId)
                .map(SkillResponse::from)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy kỹ năng", HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional
    @CacheEvict(value = {CacheConfig.CACHE_SKILLS, CacheConfig.CACHE_SKILL_DETAIL}, allEntries = true)
    public SkillResponse createSkill(SkillRequest request) {
        String trimmedName = request.getName() != null ? request.getName().trim() : "";
        if (trimmedName.isEmpty()) {
            throw new AppException("Tên kỹ năng không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (skillRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new AppException("Tên kỹ năng đã tồn tại", HttpStatus.CONFLICT);
        }
        Skill skill = Skill.create(trimmedName, request.getDescription() != null ? request.getDescription().trim() : null);
        return SkillResponse.from(skillRepository.save(skill));
    }

    @Override
    @Transactional
    @CacheEvict(value = {CacheConfig.CACHE_SKILLS, CacheConfig.CACHE_SKILL_DETAIL}, allEntries = true)
    public SkillResponse updateSkill(String skillId, SkillRequest request) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy kỹ năng", HttpStatus.NOT_FOUND));
        String trimmedName = request.getName() != null ? request.getName().trim() : "";
        if (trimmedName.isEmpty()) {
            throw new AppException("Tên kỹ năng không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (skillRepository.existsByNameIgnoreCaseAndSkillIdNot(trimmedName, skillId)) {
            throw new AppException("Tên kỹ năng đã tồn tại", HttpStatus.CONFLICT);
        }
        skill.updateSkill(trimmedName, request.getDescription() != null ? request.getDescription().trim() : null);
        return SkillResponse.from(skillRepository.save(skill));
    }

    @Override
    @Transactional
    @CacheEvict(value = {CacheConfig.CACHE_SKILLS, CacheConfig.CACHE_SKILL_DETAIL}, allEntries = true)
    public void deleteSkill(String skillId) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new AppException("Không tìm thấy kỹ năng", HttpStatus.NOT_FOUND));
        skill.softDelete();
        skillRepository.save(skill);
    }

    @Override
    @Transactional
    @CacheEvict(value = {CacheConfig.CACHE_SKILLS, CacheConfig.CACHE_SKILL_DETAIL}, allEntries = true)
    public void hardDeleteSkill(String skillId) {
        throw new AppException("Hệ thống chỉ hỗ trợ xóa mềm, không cho phép xóa cứng dữ liệu.", HttpStatus.BAD_REQUEST);
    }
}


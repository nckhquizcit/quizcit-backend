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
    public List<SkillResponse> getAllSkills() {
        return skillRepository.findAll().stream()
                .map(SkillResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SkillResponse getSkillById(String skillId) {
        return skillRepository.findById(skillId)
                .map(SkillResponse::from)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy kỹ năng", HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional
    public SkillResponse createSkill(SkillRequest request) {
        Skill skill = Skill.create(request.getName(), request.getDescription());
        return SkillResponse.from(skillRepository.save(skill));
    }

    @Override
    @Transactional
    public SkillResponse updateSkill(String skillId, SkillRequest request) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy kỹ năng", HttpStatus.NOT_FOUND));
        skill.updateSkill(request.getName(), request.getDescription());
        return SkillResponse.from(skillRepository.save(skill));
    }

    @Override
    @Transactional
    public void deleteSkill(String skillId) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new AppException("Không tìm thấy kỹ năng", HttpStatus.NOT_FOUND));
        skill.softDelete();
        skillRepository.save(skill);
    }

    @Override
    @Transactional
    public void hardDeleteSkill(String skillId) {
        if (skillRepository.findById(skillId).isEmpty()) {
            throw new AppException("Không tìm thấy kỹ năng", HttpStatus.NOT_FOUND);
        }
        questionSkillRepository.deleteBySkillId(skillId);
        chapterSkillRepository.deleteBySkillId(skillId);
        userSkillAbilityRepository.deleteBySkillId(skillId);
        abilityHistoryRepository.deleteBySkillId(skillId);
        skillRepository.hardDeleteById(skillId);
    }
}


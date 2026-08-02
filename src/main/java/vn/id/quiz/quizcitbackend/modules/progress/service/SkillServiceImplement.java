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
import vn.id.quiz.quizcitbackend.modules.content.repository.persistence.JpaChapterSkillRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.persistence.JpaAbilityHistoryRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.persistence.JpaQuestionSkillRepository;
import vn.id.quiz.quizcitbackend.modules.progress.repository.persistence.JpaUserSkillAbilityRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

@Service
@RequiredArgsConstructor
public class SkillServiceImplement implements SkillService {

    private final SkillRepository skillRepository;
    private final JpaChapterSkillRepository chapterSkillRepository;
    private final JpaQuestionSkillRepository questionSkillRepository;
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
        if (skillRepository.findById(skillId).isEmpty()) {
            throw new AppException("Không tìm thấy kỹ năng", HttpStatus.NOT_FOUND);
        }

        if (chapterSkillRepository.existsBySkillId(skillId)
                || questionSkillRepository.existsBySkillId(skillId)
                || userSkillAbilityRepository.existsBySkillId(skillId)
                || abilityHistoryRepository.existsBySkillId(skillId)) {
            throw new AppException(
                    "Không thể xóa kỹ năng đang được sử dụng. Hãy đổi tên hoặc ngừng sử dụng kỹ năng trước.",
                    HttpStatus.CONFLICT);
        }

        skillRepository.deleteById(skillId);
    }
}

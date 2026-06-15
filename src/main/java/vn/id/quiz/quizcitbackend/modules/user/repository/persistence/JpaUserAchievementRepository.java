package vn.id.quiz.quizcitbackend.modules.user.repository.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserAchievement;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserAchievementId;

import java.util.List;

public interface JpaUserAchievementRepository extends JpaRepository<UserAchievement, UserAchievementId> {
    List<UserAchievement> findByUserId(String userId);
}

package vn.id.quiz.quizcitbackend.modules.content.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.chapter.ChapterResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.page.HomeAbilityOverviewResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.page.HomeEnrolledSubjectResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.page.HomeGreetingResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.page.HomeStatsResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.page.HomeSummaryResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.subject.EnrolledSubjectResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.model.Chapter;
import vn.id.quiz.quizcitbackend.modules.content.repository.ContentRepository;
import vn.id.quiz.quizcitbackend.modules.progress.presentation.model.UserSkillAbility;
import vn.id.quiz.quizcitbackend.modules.progress.repository.ProgressRepository;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.repository.UserRepository;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PageServiceImplement implements PageService {

    private final UserRepository userRepository;
    private final ContentService contentService;
    private final CourseEnrollmentService enrollmentService;
    private final ContentRepository contentRepository;
    private final ProgressRepository progressRepository;

    @Override
    @Transactional(readOnly = true)
    public HomeSummaryResponse getHomeSummary(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new AppException("Bạn cần đăng nhập để thực hiện thao tác này.", HttpStatus.UNAUTHORIZED);
        }

        // 1. User greeting
        User user = userRepository.findUserById(userId).orElse(null);
        String displayName = user != null && user.getDisplayName() != null && !user.getDisplayName().isBlank()
                ? user.getDisplayName()
                : (user != null ? user.getUsername() : "bạn");
        String username = user != null ? user.getUsername() : "";
        String firstName = extractFirstName(displayName, username);
        String period = computeGreetingPeriod();
        HomeGreetingResponse greeting = HomeGreetingResponse.builder()
                .period(period)
                .displayName(displayName)
                .username(username)
                .firstName(firstName)
                .build();

        // 2. Ability overview (wrapped in try-catch to prevent failure from breaking home page)
        HomeAbilityOverviewResponse abilityOverview;
        try {
            List<UserSkillAbility> abilities = progressRepository.findUserSkillAbilities(userId);
            int skillCount = abilities.size();
            Double averageTheta = skillCount > 0 ? progressRepository.getAverageAbility(userId) : null;
            String abilityLevel = averageTheta != null ? computeAbilityLevelLabel(averageTheta) : null;
            abilityOverview = HomeAbilityOverviewResponse.builder()
                    .averageTheta(averageTheta)
                    .skillCount(skillCount)
                    .abilityLevel(abilityLevel)
                    .build();
        } catch (Exception ex) {
            log.warn("Failed to compute ability overview for user {}: {}", userId, ex.getMessage());
            abilityOverview = HomeAbilityOverviewResponse.builder()
                    .averageTheta(null)
                    .skillCount(0)
                    .abilityLevel(null)
                    .build();
        }

        // 3. Enrolled subjects and chapter counts
        List<EnrolledSubjectResponse> enrollments = enrollmentService.getEnrolledSubjects(userId);
        List<ChapterResponse> accessibleChapters = contentService.getAllAccessibleChapters();
        Map<String, Long> unlockedCountBySubject = accessibleChapters.stream()
                .collect(Collectors.groupingBy(ChapterResponse::subjectId, Collectors.counting()));

        List<String> subjectIds = enrollments.stream().map(EnrolledSubjectResponse::subjectId).toList();
        Map<String, Long> totalChaptersBySubject = subjectIds.isEmpty() ? Map.of() :
                contentRepository.findAllChapters(subjectIds).stream()
                        .collect(Collectors.groupingBy(Chapter::getSubjectId, Collectors.counting()));

        List<HomeEnrolledSubjectResponse> enrolledSubjects = enrollments.stream().map(sub -> {
            int unlocked = unlockedCountBySubject.getOrDefault(sub.subjectId(), 0L).intValue();
            int total = totalChaptersBySubject.getOrDefault(sub.subjectId(), 0L).intValue();
            return HomeEnrolledSubjectResponse.builder()
                    .subjectId(sub.subjectId())
                    .courseCode(sub.courseCode())
                    .name(sub.name())
                    .description(sub.description())
                    .imageUrl(sub.imageUrl())
                    .displayOrder(sub.displayOrder())
                    .chapterCount(unlocked)
                    .unlockedChapterCount(unlocked)
                    .totalChapterCount(total)
                    .enrolledAt(sub.enrolledAt())
                    .build();
        }).toList();

        // 4. Stats
        HomeStatsResponse stats = HomeStatsResponse.builder()
                .totalEnrolledSubjects(enrolledSubjects.size())
                .totalUnlockedChapters(accessibleChapters.size())
                .build();

        return HomeSummaryResponse.builder()
                .greeting(greeting)
                .abilityOverview(abilityOverview)
                .enrolledSubjects(enrolledSubjects)
                .stats(stats)
                .build();
    }

    private String extractFirstName(String displayName, String username) {
        String trimmed = (displayName != null && !displayName.isBlank()) ? displayName.trim() : "";
        if (trimmed.isEmpty()) {
            trimmed = (username != null && !username.isBlank()) ? username.trim() : "bạn";
        }
        String[] parts = trimmed.split("\\s+");
        return parts[parts.length - 1];
    }

    private String computeGreetingPeriod() {
        int hour = LocalTime.now().getHour();
        if (hour < 11) return "sáng";
        if (hour < 18) return "chiều";
        return "tối";
    }

    private String computeAbilityLevelLabel(double theta) {
        if (theta < -1.5) return "Yếu";
        if (theta < -0.5) return "Trung bình";
        if (theta < 0.5) return "Trung bình khá";
        if (theta < 1.5) return "Khá";
        if (theta < 2.25) return "Giỏi";
        return "Xuất sắc";
    }
}

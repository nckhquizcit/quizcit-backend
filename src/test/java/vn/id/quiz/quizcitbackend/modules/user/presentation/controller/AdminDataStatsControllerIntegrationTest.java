package vn.id.quiz.quizcitbackend.modules.user.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootTest
class AdminDataStatsControllerIntegrationTest {
    @Autowired AdminDataStatsController controller;

    @Test
    void aggregateStatsQueryRunsAgainstTheConfiguredPostgresSchema() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "integration-admin", null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRATOR"))));
        try {
            var stats = controller.getDataStats().data();
            assertThat(stats.totalUsers()).isNotNegative();
            assertThat(stats.totalSubjects()).isNotNegative();
            assertThat(stats.totalQuestions()).isNotNegative();
            assertThat(stats.totalQuizzes()).isNotNegative();
            assertThat(stats.totalAttempts()).isNotNegative();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}

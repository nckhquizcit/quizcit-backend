package vn.id.quiz.quizcitbackend.modules.user.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;

class AdminDataStatsControllerTest {

    @Test
    void aggregateStatsMapsDatabaseCounts() {
        EntityManager entityManager = mock(EntityManager.class);
        Query query = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(new Object[]{11L, 12L, 13L, 14L, 15L});

        var stats = new AdminDataStatsController(entityManager).getDataStats().data();

        assertThat(stats.totalUsers()).isEqualTo(11);
        assertThat(stats.totalSubjects()).isEqualTo(12);
        assertThat(stats.totalQuestions()).isEqualTo(13);
        assertThat(stats.totalQuizzes()).isEqualTo(14);
        assertThat(stats.totalAttempts()).isEqualTo(15);
    }
}

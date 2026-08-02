package vn.id.quiz.quizcitbackend.shared.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.SocketException;
import java.sql.SQLException;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.Test;

class FlywayRetryConfigTest {
    @Test
    void identifiesPostgresConnectionFailuresAsTransient() {
        var exception = new FlywayException("migration failed",
                new SQLException("connection reset", "08006"));

        assertTrue(FlywayRetryConfig.isTransientConnectionFailure(exception));
    }

    @Test
    void identifiesNestedSocketFailureAsTransient() {
        var exception = new FlywayException("migration failed",
                new RuntimeException(new SocketException("Connection reset")));

        assertTrue(FlywayRetryConfig.isTransientConnectionFailure(exception));
    }

    @Test
    void doesNotRetryRealMigrationErrors() {
        var exception = new FlywayException("bad SQL",
                new SQLException("syntax error", "42601"));

        assertFalse(FlywayRetryConfig.isTransientConnectionFailure(exception));
    }

    @Test
    void retriesPostgresAdvisoryLockContention() {
        var exception = new FlywayException(
                "Number of retries exceeded while attempting to acquire PostgreSQL advisory lock");

        assertTrue(FlywayRetryConfig.isTransientConnectionFailure(exception));
    }
}

package vn.id.quiz.quizcitbackend.shared.config;

import java.net.SocketException;
import java.sql.SQLException;

import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Retry Flyway startup when a remote PostgreSQL connection is reset. */
@Configuration
public class FlywayRetryConfig {
    private static final Logger log = LoggerFactory.getLogger(FlywayRetryConfig.class);

    @Bean
    FlywayMigrationStrategy retryingFlywayMigrationStrategy(
            @Value("${app.database.migration.max-attempts:4}") int maxAttempts,
            @Value("${app.database.migration.retry-delay-ms:1500}") long retryDelayMs) {
        int safeMaxAttempts = Math.max(1, maxAttempts);
        long safeDelay = Math.max(0, retryDelayMs);

        return flyway -> {
            for (int attempt = 1; attempt <= safeMaxAttempts; attempt++) {
                try {
                    // Flyway 10 acquires a PostgreSQL advisory lock even when
                    // the schema is already current. Avoid that remote lock on
                    // the overwhelmingly common no-op startup path.
                    if (flyway.info().pending().length == 0) {
                        flyway.validate();
                        log.info("Database schema đã ở phiên bản mới nhất; bỏ qua migration lock");
                        return;
                    }
                    flyway.migrate();
                    return;
                } catch (FlywayException exception) {
                    if (!isTransientConnectionFailure(exception) || attempt == safeMaxAttempts) {
                        throw exception;
                    }
                    log.warn("Kết nối database bị gián đoạn khi chạy Flyway; thử lại lần {}/{} sau {} ms",
                            attempt + 1, safeMaxAttempts, safeDelay);
                    sleep(safeDelay, exception);
                }
            }
        };
    }

    static boolean isTransientConnectionFailure(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.contains(
                    "Number of retries exceeded while attempting to acquire PostgreSQL advisory lock")) {
                return true;
            }
            if (current instanceof SQLException sqlException) {
                String sqlState = sqlException.getSQLState();
                if (sqlState != null && sqlState.startsWith("08")) {
                    return true;
                }
            }
            if (current instanceof SocketException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static void sleep(long delayMs, FlywayException original) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            original.addSuppressed(interrupted);
            throw original;
        }
    }
}

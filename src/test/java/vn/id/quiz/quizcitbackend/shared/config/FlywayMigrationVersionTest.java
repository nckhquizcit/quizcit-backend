package vn.id.quiz.quizcitbackend.shared.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class FlywayMigrationVersionTest {
    private static final Pattern VERSIONED_MIGRATION = Pattern.compile("^V([^_]+)__.*\\.sql$");

    @Test
    void versionedMigrationsHaveUniqueVersions() throws IOException {
        Map<String, List<String>> filesByVersion;
        try (var files = Files.list(Path.of("src/main/resources/db/migration"))) {
            filesByVersion = files.map(path -> path.getFileName().toString())
                    .filter(name -> VERSIONED_MIGRATION.matcher(name).matches())
                    .collect(Collectors.groupingBy(name -> {
                        var matcher = VERSIONED_MIGRATION.matcher(name);
                        matcher.matches();
                        return matcher.group(1);
                    }));
        }

        Map<String, List<String>> duplicates = filesByVersion.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertThat(duplicates).as("duplicate Flyway versions").isEmpty();
    }
}

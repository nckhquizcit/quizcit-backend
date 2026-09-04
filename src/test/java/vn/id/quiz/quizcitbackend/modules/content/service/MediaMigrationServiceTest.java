package vn.id.quiz.quizcitbackend.modules.content.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.MediaMigrationReport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaMigrationServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @TempDir
    Path tempDir;

    private MediaMigrationService migrationService;

    @BeforeEach
    void setUp() {
        migrationService = new MediaMigrationService(cloudinary, jdbcTemplate, tempDir.toString());
    }

    @Test
    void migrateLocalUploadsShouldScanFilesAndUploadAndUpdateDb() throws IOException {
        Path subjectsDir = tempDir.resolve("subjects");
        Files.createDirectories(subjectsDir);
        Path sampleFile = subjectsDir.resolve("sample.webp");
        Files.write(sampleFile, new byte[]{1, 2, 3, 4});

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(Map.of(
                "secure_url", "https://res.cloudinary.com/demo/image/upload/v1/quizcit/subjects/sample.webp",
                "public_id", "quizcit/subjects/sample"
        ));
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any())).thenReturn(1);

        MediaMigrationReport report = migrationService.migrateLocalUploadsToCloudinary();

        assertThat(report.totalFilesFound()).isEqualTo(1);
        assertThat(report.migratedFiles()).isEqualTo(1);
        assertThat(report.failedFiles()).isEqualTo(0);
        assertThat(report.updatedSubjectRows()).isEqualTo(1);
        assertThat(report.updatedUserRows()).isEqualTo(1);
        assertThat(report.details()).hasSize(1);
        assertThat(report.details().get(0).status()).isEqualTo("SUCCESS");
        assertThat(report.details().get(0).cloudinaryUrl()).contains("cloudinary.com");
    }

    @Test
    void migrateLocalUploadsShouldHandleEmptyDir() {
        MediaMigrationReport report = migrationService.migrateLocalUploadsToCloudinary();
        assertThat(report.totalFilesFound()).isEqualTo(0);
        assertThat(report.migratedFiles()).isEqualTo(0);
    }
}

package vn.id.quiz.quizcitbackend.modules.content.service;

import com.cloudinary.Cloudinary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.MediaMigrationReport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@Slf4j
public class MediaMigrationService {

    private final Cloudinary cloudinary;
    private final JdbcTemplate jdbcTemplate;
    private final Path rootUploadDir;

    public MediaMigrationService(
            Cloudinary cloudinary,
            JdbcTemplate jdbcTemplate,
            @Value("${quizcit.upload.dir:uploads}") String uploadDir
    ) {
        this.cloudinary = cloudinary;
        this.jdbcTemplate = jdbcTemplate;
        this.rootUploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    /**
     * Quét các tập tin hình ảnh/video trong thư mục uploads/ cục bộ,
     * tải lên Cloudinary và cập nhật lại đường dẫn trong cơ sở dữ liệu.
     */
    @Transactional
    public MediaMigrationReport migrateLocalUploadsToCloudinary() {
        if (!Files.exists(rootUploadDir)) {
            log.warn("Thư mục uploads không tồn tại tại: {}", rootUploadDir);
            return new MediaMigrationReport(0, 0, 0, 0, 0, 0, 0, List.of());
        }

        List<Path> files = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(rootUploadDir)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> !path.getFileName().toString().startsWith("."))
                    .forEach(files::add);
        } catch (IOException e) {
            log.error("Không thể duyệt thư mục uploads: {}", rootUploadDir, e);
            return new MediaMigrationReport(0, 0, 0, 0, 0, 0, 0, List.of());
        }

        int migratedFiles = 0;
        int failedFiles = 0;
        int totalSubjectsUpdated = 0;
        int totalUsersUpdated = 0;
        int totalTheoriesUpdated = 0;
        int totalQuestionsUpdated = 0;
        List<MediaMigrationReport.FileMigrationDetail> details = new ArrayList<>();

        for (Path file : files) {
            Path relativePath = rootUploadDir.relativize(file);
            String folder = "subjects";
            String fileName = file.getFileName().toString();

            if (relativePath.getNameCount() >= 2) {
                folder = relativePath.getName(0).toString().toLowerCase();
            }

            String publicId = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;

            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> uploadResult = cloudinary.uploader().upload(
                        Files.readAllBytes(file),
                        Map.of(
                                "folder", "quizcit/" + folder,
                                "public_id", publicId,
                                "overwrite", true,
                                "resource_type", "auto"
                        )
                );

                String secureUrl = (String) uploadResult.get("secure_url");
                if (secureUrl == null || secureUrl.isBlank()) {
                    secureUrl = (String) uploadResult.get("url");
                }

                // Cập nhật database
                int subjectsUpdated = updateSubjectImageUrl(folder, fileName, secureUrl);
                int usersUpdated = updateUserAvatarUrl(folder, fileName, secureUrl);
                int theoriesUpdated = updateContentReferences("theories", folder, fileName, secureUrl);
                int questionsUpdated = updateContentReferences("questions", folder, fileName, secureUrl);

                totalSubjectsUpdated += subjectsUpdated;
                totalUsersUpdated += usersUpdated;
                totalTheoriesUpdated += theoriesUpdated;
                totalQuestionsUpdated += questionsUpdated;

                migratedFiles++;
                details.add(new MediaMigrationReport.FileMigrationDetail(
                        folder,
                        fileName,
                        relativePath.toString(),
                        secureUrl,
                        "SUCCESS",
                        null
                ));

                log.info("Migrated {} to Cloudinary: {} (subjects: {}, users: {}, theories: {}, questions: {})",
                        relativePath, secureUrl, subjectsUpdated, usersUpdated, theoriesUpdated, questionsUpdated);
            } catch (Exception e) {
                failedFiles++;
                log.error("Failed to migrate file {} to Cloudinary: {}", relativePath, e.getMessage(), e);
                details.add(new MediaMigrationReport.FileMigrationDetail(
                        folder,
                        fileName,
                        relativePath.toString(),
                        null,
                        "FAILED",
                        e.getMessage()
                ));
            }
        }

        return new MediaMigrationReport(
                files.size(),
                migratedFiles,
                failedFiles,
                totalSubjectsUpdated,
                totalUsersUpdated,
                totalTheoriesUpdated,
                totalQuestionsUpdated,
                details
        );
    }

    private int updateSubjectImageUrl(String folder, String fileName, String newUrl) {
        try {
            String pattern1 = "%/api/uploads/" + folder + "/" + fileName + "%";
            String pattern2 = "%/uploads/" + folder + "/" + fileName + "%";
            String pattern3 = "%" + fileName;

            String sql = "UPDATE subjects SET image_url = ? WHERE (image_url NOT LIKE '%res.cloudinary.com%') AND (image_url LIKE ? OR image_url LIKE ? OR image_url LIKE ?)";
            return jdbcTemplate.update(sql, newUrl, pattern1, pattern2, pattern3);
        } catch (Exception e) {
            log.warn("Could not update image_url in subjects table: {}", e.getMessage());
            return 0;
        }
    }

    private int updateUserAvatarUrl(String folder, String fileName, String newUrl) {
        try {
            String pattern1 = "%/api/uploads/" + folder + "/" + fileName + "%";
            String pattern2 = "%/uploads/" + folder + "/" + fileName + "%";
            String pattern3 = "%" + fileName;

            String sql = "UPDATE users SET avatar_url = ? WHERE (avatar_url NOT LIKE '%res.cloudinary.com%') AND (avatar_url LIKE ? OR avatar_url LIKE ? OR avatar_url LIKE ?)";
            return jdbcTemplate.update(sql, newUrl, pattern1, pattern2, pattern3);
        } catch (Exception e) {
            log.warn("Could not update avatar_url in users table: {}", e.getMessage());
            return 0;
        }
    }

    private int updateContentReferences(String table, String folder, String fileName, String newUrl) {
        int count = 0;
        String[] oldPaths = {
                "/api/uploads/" + folder + "/" + fileName,
                "/uploads/" + folder + "/" + fileName
        };

        for (String oldPath : oldPaths) {
            String pattern = "%" + oldPath + "%";
            try {
                String sql = "UPDATE " + table + " SET content = REPLACE(content, ?, ?) WHERE content LIKE ?";
                count += jdbcTemplate.update(sql, oldPath, newUrl, pattern);
            } catch (Exception e) {
                log.warn("Could not update content references in table {}: {}", table, e.getMessage());
            }
        }
        return count;
    }
}

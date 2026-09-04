package vn.id.quiz.quizcitbackend.modules.content.presentation.dto;

import java.util.List;

public record MediaMigrationReport(
        int totalFilesFound,
        int migratedFiles,
        int failedFiles,
        int updatedSubjectRows,
        int updatedUserRows,
        int updatedTheoryRows,
        int updatedQuestionRows,
        List<FileMigrationDetail> details
) {
    public record FileMigrationDetail(
            String folder,
            String fileName,
            String localPath,
            String cloudinaryUrl,
            String status,
            String error
    ) {}
}

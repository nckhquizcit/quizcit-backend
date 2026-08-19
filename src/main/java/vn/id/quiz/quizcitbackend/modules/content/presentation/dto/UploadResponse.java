package vn.id.quiz.quizcitbackend.modules.content.presentation.dto;

public record UploadResponse(
        String url,
        String fileName,
        long fileSize,
        String contentType
) {
}

package vn.id.quiz.quizcitbackend.modules.content.service;

import com.cloudinary.Cloudinary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class MediaUploadService {

    private final Cloudinary cloudinary;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp",
            "image/gif",
            "image/svg+xml"
    );

    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
            "video/mp4",
            "video/webm",
            "video/ogg",
            "video/quicktime",
            "video/x-msvideo",
            "video/mpeg",
            "video/3gpp"
    );

    private static final Set<String> ALLOWED_AUDIO_TYPES = Set.of(
            "audio/mpeg",
            "audio/mp3",
            "audio/wav",
            "audio/ogg",
            "audio/webm"
    );

    public MediaUploadService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Tải tệp hình ảnh hoặc video lên Cloudinary.
     */
    public String saveMedia(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new AppException("Tập tin tải lên không được để trống", HttpStatus.BAD_REQUEST);
        }

        String contentType = file.getContentType();
        if (!isSupportedMediaType(contentType)) {
            throw new AppException(
                    "Định dạng tập tin không được hỗ trợ. Vui lòng chọn ảnh (JPG, PNG, WEBP, GIF, SVG) hoặc video (MP4, WEBM, MOV, AVI).",
                    HttpStatus.BAD_REQUEST
            );
        }

        String sanitizedFolder = sanitizeFolderName(folder);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of(
                            "folder", "quizcit/" + sanitizedFolder,
                            "resource_type", "auto"
                    )
            );

            String secureUrl = (String) uploadResult.get("secure_url");
            if (secureUrl == null || secureUrl.isBlank()) {
                secureUrl = (String) uploadResult.get("url");
            }
            if (secureUrl == null || secureUrl.isBlank()) {
                throw new AppException("Không nhận được URL từ dịch vụ lưu trữ Cloudinary", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            log.info("Uploaded media to Cloudinary: {} (public_id: {})", secureUrl, uploadResult.get("public_id"));
            return secureUrl;
        } catch (IOException e) {
            log.error("Cloudinary upload failed for folder {}: {}", sanitizedFolder, e.getMessage(), e);
            throw new AppException("Không thể tải tập tin lên dịch vụ lưu trữ: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Tương thích ngược với các nơi gọi saveImage.
     */
    public String saveImage(MultipartFile file, String folder) {
        return saveMedia(file, folder);
    }

    /**
     * Tải tệp phương tiện trực tiếp từ URL bên ngoài lên Cloudinary.
     */
    public String saveMediaFromUrl(String sourceUrl, String folder) {
        if (sourceUrl == null || sourceUrl.isBlank()) {
            throw new AppException("URL hình ảnh không được để trống", HttpStatus.BAD_REQUEST);
        }

        String trimmedUrl = sourceUrl.trim();
        URI uri;
        try {
            uri = URI.create(trimmedUrl);
        } catch (IllegalArgumentException e) {
            throw new AppException("URL hình ảnh không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new AppException("Chỉ hỗ trợ URL giao thức HTTP hoặc HTTPS", HttpStatus.BAD_REQUEST);
        }

        String sanitizedFolder = sanitizeFolderName(folder);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    trimmedUrl,
                    Map.of(
                            "folder", "quizcit/" + sanitizedFolder,
                            "resource_type", "auto"
                    )
            );

            String secureUrl = (String) uploadResult.get("secure_url");
            if (secureUrl == null || secureUrl.isBlank()) {
                secureUrl = (String) uploadResult.get("url");
            }
            if (secureUrl == null || secureUrl.isBlank()) {
                throw new AppException("Không nhận được URL từ dịch vụ lưu trữ Cloudinary", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            log.info("Uploaded media from URL to Cloudinary: {} (public_id: {})", secureUrl, uploadResult.get("public_id"));
            return secureUrl;
        } catch (Exception e) {
            log.error("Cloudinary upload from URL failed for url {}: {}", trimmedUrl, e.getMessage(), e);
            throw new AppException("Không thể tải ảnh từ URL: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Tương thích ngược với các nơi gọi saveImageFromUrl.
     */
    public String saveImageFromUrl(String sourceUrl, String folder) {
        return saveMediaFromUrl(sourceUrl, folder);
    }

    /**
     * Tạo URL bảo mật của Cloudinary tương ứng với tài nguyên (dùng để chuyển hướng yêu cầu cũ nếu có).
     */
    public String getCloudinaryMediaUrl(String folder, String fileName) {
        String sanitizedFolder = sanitizeFolderName(folder);
        return cloudinary.url().secure(true).generate("quizcit/" + sanitizedFolder + "/" + fileName);
    }

    public boolean isSupportedMediaType(String contentType) {
        if (contentType == null) return false;
        String normalized = contentType.toLowerCase().trim();
        if (normalized.contains(";")) {
            normalized = normalized.substring(0, normalized.indexOf(';')).trim();
        }
        return ALLOWED_IMAGE_TYPES.contains(normalized)
                || ALLOWED_VIDEO_TYPES.contains(normalized)
                || ALLOWED_AUDIO_TYPES.contains(normalized);
    }

    private String sanitizeFolderName(String folder) {
        if (folder == null || folder.isBlank()) {
            return "subjects";
        }
        return folder.replaceAll("[^a-zA-Z0-9_-]", "").toLowerCase();
    }
}

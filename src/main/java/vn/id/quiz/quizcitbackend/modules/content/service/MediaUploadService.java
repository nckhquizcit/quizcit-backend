package vn.id.quiz.quizcitbackend.modules.content.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class MediaUploadService {

    private final Path rootUploadDir;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp",
            "image/gif",
            "image/svg+xml"
    );

    private static final Map<String, String> MIME_TO_EXT = Map.of(
            "image/jpeg", ".jpg",
            "image/jpg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif",
            "image/svg+xml", ".svg"
    );

    public MediaUploadService(@Value("${quizcit.upload.dir:uploads}") String uploadDir) {
        this.rootUploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootUploadDir);
        } catch (IOException e) {
            log.error("Could not initialize upload directory: {}", this.rootUploadDir, e);
        }
    }

    public String saveImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new AppException("Tập tin tải lên không được để trống", HttpStatus.BAD_REQUEST);
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new AppException("Định dạng tập tin không được hỗ trợ. Vui lòng chọn ảnh JPG, PNG, WEBP, GIF hoặc SVG.", HttpStatus.BAD_REQUEST);
        }

        String sanitizedFolder = sanitizeFolderName(folder);
        Path targetDir = rootUploadDir.resolve(sanitizedFolder).normalize();

        if (!targetDir.startsWith(rootUploadDir)) {
            throw new AppException("Thư mục lưu trữ không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            log.error("Could not create target directory: {}", targetDir, e);
            throw new AppException("Không thể tạo thư mục lưu trữ tập tin", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String extension = getExtension(file.getOriginalFilename(), contentType);
        String fileName = UUID.randomUUID().toString() + extension;
        Path targetFilePath = targetDir.resolve(fileName).normalize();

        try {
            Files.copy(file.getInputStream(), targetFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Could not save file: {}", targetFilePath, e);
            throw new AppException("Không thể lưu tập tin tải lên", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return "/api/uploads/" + sanitizedFolder + "/" + fileName;
    }

    public String saveImageFromUrl(String sourceUrl, String folder) {
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

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "QuizCit/1.0 (ImageFetcher)")
                    .header("Accept", "image/*")
                    .GET()
                    .build();

            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new AppException("Không thể tải ảnh từ URL (Mã phản hồi HTTP: " + response.statusCode() + ")", HttpStatus.BAD_REQUEST);
            }

            byte[] body = response.body();
            if (body == null || body.length == 0) {
                throw new AppException("Dữ liệu ảnh từ URL rỗng", HttpStatus.BAD_REQUEST);
            }

            if (body.length > 10 * 1024 * 1024) {
                throw new AppException("Kích thước ảnh từ URL vượt quá giới hạn cho phép (10MB)", HttpStatus.BAD_REQUEST);
            }

            String contentType = response.headers().firstValue("Content-Type").orElse("image/jpeg");
            if (contentType.contains(";")) {
                contentType = contentType.substring(0, contentType.indexOf(';')).trim();
            }

            if (!ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
                String path = uri.getPath();
                String extFromPath = (path != null && path.contains(".")) ? path.substring(path.lastIndexOf('.')).toLowerCase() : "";
                if (extFromPath.equals(".png") || extFromPath.equals(".webp") || extFromPath.equals(".gif") || extFromPath.equals(".svg") || extFromPath.equals(".jpg") || extFromPath.equals(".jpeg")) {
                    contentType = switch (extFromPath) {
                        case ".png" -> "image/png";
                        case ".webp" -> "image/webp";
                        case ".gif" -> "image/gif";
                        case ".svg" -> "image/svg+xml";
                        default -> "image/jpeg";
                    };
                } else {
                    throw new AppException("URL không trả về định dạng ảnh hợp lệ (Content-Type: " + contentType + ")", HttpStatus.BAD_REQUEST);
                }
            }

            String sanitizedFolder = sanitizeFolderName(folder);
            Path targetDir = rootUploadDir.resolve(sanitizedFolder).normalize();

            if (!targetDir.startsWith(rootUploadDir)) {
                throw new AppException("Thư mục lưu trữ không hợp lệ", HttpStatus.BAD_REQUEST);
            }

            Files.createDirectories(targetDir);

            String extension = MIME_TO_EXT.getOrDefault(contentType.toLowerCase(), ".jpg");
            String fileName = UUID.randomUUID().toString() + extension;
            Path targetFilePath = targetDir.resolve(fileName).normalize();

            Files.write(targetFilePath, body);

            return "/api/uploads/" + sanitizedFolder + "/" + fileName;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error downloading image from URL: {}", trimmedUrl, e);
            throw new AppException("Không thể tải ảnh từ URL: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public Resource loadFileAsResource(String folder, String fileName) {
        try {
            String sanitizedFolder = sanitizeFolderName(folder);
            Path filePath = rootUploadDir.resolve(sanitizedFolder).resolve(fileName).normalize();

            if (!filePath.startsWith(rootUploadDir)) {
                throw new AppException("Đường dẫn tập tin không hợp lệ", HttpStatus.BAD_REQUEST);
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new AppException("Không tìm thấy tập tin", HttpStatus.NOT_FOUND);
            }
        } catch (MalformedURLException e) {
            throw new AppException("Không tìm thấy tập tin", HttpStatus.NOT_FOUND);
        }
    }

    private String sanitizeFolderName(String folder) {
        if (folder == null || folder.isBlank()) {
            return "subjects";
        }
        return folder.replaceAll("[^a-zA-Z0-9_-]", "").toLowerCase();
    }

    private String getExtension(String originalFilename, String contentType) {
        if (originalFilename != null && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (ext.length() <= 5) {
                return ext;
            }
        }
        return MIME_TO_EXT.getOrDefault(contentType.toLowerCase(), ".jpg");
    }
}

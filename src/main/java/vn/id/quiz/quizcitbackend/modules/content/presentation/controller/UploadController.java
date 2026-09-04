package vn.id.quiz.quizcitbackend.modules.content.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.MediaMigrationReport;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.UploadResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.UploadUrlRequest;
import vn.id.quiz.quizcitbackend.modules.content.service.MediaMigrationService;
import vn.id.quiz.quizcitbackend.modules.content.service.MediaUploadService;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

import java.net.URI;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UploadController {

    private final MediaUploadService uploadService;
    private final MediaMigrationService migrationService;

    @PostMapping("/admin/uploads/migrate-to-cloudinary")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<MediaMigrationReport>> migrateUploadsToCloudinary() {
        MediaMigrationReport report = migrationService.migrateLocalUploadsToCloudinary();
        return ResponseEntity.ok(ApiResponse.success("Di chuyển dữ liệu hình ảnh sang Cloudinary thành công", report));
    }

    @PostMapping(value = {"/uploads/image", "/upload/image", "/uploads/media", "/upload/media"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'USER')")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "subjects") String folder,
            Authentication authentication
    ) {
        String effectiveFolder = folder;
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATOR"));
        if (!isAdmin) {
            effectiveFolder = "avatars";
        }
        String url = uploadService.saveMedia(file, effectiveFolder);
        String fileName = url.substring(url.lastIndexOf('/') + 1);

        UploadResponse response = new UploadResponse(
                url,
                fileName,
                file.getSize(),
                file.getContentType()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tải tệp lên thành công", response));
    }

    @PostMapping(value = {"/uploads/from-url", "/upload/from-url"})
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadFromUrl(
            @Valid @RequestBody UploadUrlRequest request
    ) {
        String folder = request.folder() != null && !request.folder().isBlank() ? request.folder() : "subjects";
        String url = uploadService.saveMediaFromUrl(request.url(), folder);
        String fileName = url.substring(url.lastIndexOf('/') + 1);

        UploadResponse response = new UploadResponse(
                url,
                fileName,
                0,
                "image/jpeg"
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tải tệp từ URL thành công", response));
    }

    @GetMapping("/uploads/{folder}/{fileName:.+}")
    public ResponseEntity<Void> getUploadedFile(
            @PathVariable String folder,
            @PathVariable String fileName
    ) {
        String redirectUrl = uploadService.getCloudinaryMediaUrl(folder, fileName);
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .location(URI.create(redirectUrl))
                .build();
    }

    @GetMapping("/uploads/{fileName:.+}")
    public ResponseEntity<Void> getUploadedFileDefaultFolder(
            @PathVariable String fileName
    ) {
        return getUploadedFile("subjects", fileName);
    }
}

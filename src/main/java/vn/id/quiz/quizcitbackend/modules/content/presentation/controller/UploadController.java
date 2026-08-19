package vn.id.quiz.quizcitbackend.modules.content.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.UploadResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.UploadUrlRequest;
import vn.id.quiz.quizcitbackend.modules.content.service.MediaUploadService;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UploadController {

    private final MediaUploadService uploadService;

    @PostMapping(value = {"/uploads/image", "/upload/image"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "subjects") String folder
    ) {
        String url = uploadService.saveImage(file, folder);
        String fileName = url.substring(url.lastIndexOf('/') + 1);

        UploadResponse response = new UploadResponse(
                url,
                fileName,
                file.getSize(),
                file.getContentType()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tải ảnh lên thành công", response));
    }

    @PostMapping(value = {"/uploads/from-url", "/upload/from-url"})
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadFromUrl(
            @Valid @RequestBody UploadUrlRequest request
    ) {
        String folder = request.folder() != null && !request.folder().isBlank() ? request.folder() : "subjects";
        String url = uploadService.saveImageFromUrl(request.url(), folder);
        String fileName = url.substring(url.lastIndexOf('/') + 1);

        UploadResponse response = new UploadResponse(
                url,
                fileName,
                0,
                "image/jpeg"
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tải ảnh từ URL thành công", response));
    }

    @GetMapping("/uploads/{folder}/{fileName:.+}")
    public ResponseEntity<Resource> getUploadedFile(
            @PathVariable String folder,
            @PathVariable String fileName
    ) {
        Resource resource = uploadService.loadFileAsResource(folder, fileName);
        MediaType mediaType = MediaTypeFactory.getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .body(resource);
    }

    @GetMapping("/uploads/{fileName:.+}")
    public ResponseEntity<Resource> getUploadedFileDefaultFolder(
            @PathVariable String fileName
    ) {
        return getUploadedFile("subjects", fileName);
    }
}

package vn.id.quiz.quizcitbackend.modules.content.presentation.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.MediaMigrationReport;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.UploadResponse;
import vn.id.quiz.quizcitbackend.modules.content.presentation.dto.UploadUrlRequest;
import vn.id.quiz.quizcitbackend.modules.content.service.MediaMigrationService;
import vn.id.quiz.quizcitbackend.modules.content.service.MediaUploadService;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UploadControllerTest {

    private MediaUploadService uploadService;
    private MediaMigrationService migrationService;
    private UploadController controller;

    @BeforeEach
    void setUp() {
        uploadService = mock(MediaUploadService.class);
        migrationService = mock(MediaMigrationService.class);
        controller = new UploadController(uploadService, migrationService);
    }

    @Test
    void uploadImageShouldUploadToCloudinaryAndReturnCreated() {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[]{1, 2, 3});
        Authentication auth = mock(Authentication.class);
        when(auth.getAuthorities()).thenAnswer(inv -> List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(uploadService.saveMedia(any(), eq("avatars")))
                .thenReturn("https://res.cloudinary.com/p8jw1ylz/image/upload/quizcit/avatars/avatar.png");

        ResponseEntity<ApiResponse<UploadResponse>> response = controller.uploadImage(file, "subjects", auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data().url()).contains("cloudinary.com");
    }

    @Test
    void uploadFromUrlShouldUploadToCloudinaryAndReturnCreated() {
        UploadUrlRequest request = new UploadUrlRequest("https://example.com/banner.jpg", "subjects");
        when(uploadService.saveMediaFromUrl("https://example.com/banner.jpg", "subjects"))
                .thenReturn("https://res.cloudinary.com/p8jw1ylz/image/upload/quizcit/subjects/banner.jpg");

        ResponseEntity<ApiResponse<UploadResponse>> response = controller.uploadFromUrl(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().data().url()).isEqualTo("https://res.cloudinary.com/p8jw1ylz/image/upload/quizcit/subjects/banner.jpg");
    }

    @Test
    void getUploadedFileShouldRedirectToCloudinaryWith301() {
        when(uploadService.getCloudinaryMediaUrl("subjects", "sample.webp"))
                .thenReturn("https://res.cloudinary.com/p8jw1ylz/image/upload/quizcit/subjects/sample.webp");

        ResponseEntity<Void> response = controller.getUploadedFile("subjects", "sample.webp");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.MOVED_PERMANENTLY);
        assertThat(response.getHeaders().getLocation()).isEqualTo(URI.create("https://res.cloudinary.com/p8jw1ylz/image/upload/quizcit/subjects/sample.webp"));
    }

    @Test
    void migrateUploadsToCloudinaryShouldReturnReport() {
        MediaMigrationReport mockReport = new MediaMigrationReport(2, 2, 0, 2, 0, 0, 0, List.of());
        when(migrationService.migrateLocalUploadsToCloudinary()).thenReturn(mockReport);

        ResponseEntity<ApiResponse<MediaMigrationReport>> response = controller.migrateUploadsToCloudinary();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data().migratedFiles()).isEqualTo(2);
    }
}

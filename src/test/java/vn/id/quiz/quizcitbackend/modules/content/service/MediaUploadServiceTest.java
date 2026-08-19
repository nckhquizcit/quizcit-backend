package vn.id.quiz.quizcitbackend.modules.content.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MediaUploadServiceTest {

    @TempDir
    Path tempDir;

    private MediaUploadService service;

    @BeforeEach
    void setUp() {
        service = new MediaUploadService(tempDir.toString());
    }

    @Test
    void saveImageShouldSaveFileAndReturnUrl() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-subject.png",
                "image/png",
                new byte[]{1, 2, 3, 4}
        );

        String url = service.saveImage(file, "subjects");

        assertThat(url).startsWith("/api/uploads/subjects/");
        assertThat(url).endsWith(".png");

        String fileName = url.substring(url.lastIndexOf('/') + 1);
        Resource resource = service.loadFileAsResource("subjects", fileName);
        assertThat(resource.exists()).isTrue();
        assertThat(resource.isReadable()).isTrue();
    }

    @Test
    void saveImageShouldThrowWhenFileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.png",
                "image/png",
                new byte[]{}
        );

        assertThatThrownBy(() -> service.saveImage(emptyFile, "subjects"))
                .isInstanceOf(AppException.class)
                .hasMessage("Tập tin tải lên không được để trống");
    }

    @Test
    void saveImageShouldThrowWhenContentTypeNotAllowed() {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                new byte[]{1, 2, 3}
        );

        assertThatThrownBy(() -> service.saveImage(pdfFile, "subjects"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Định dạng tập tin không được hỗ trợ");
    }

    @Test
    void saveImageFromUrlShouldThrowWhenUrlIsBlank() {
        assertThatThrownBy(() -> service.saveImageFromUrl("", "subjects"))
                .isInstanceOf(AppException.class)
                .hasMessage("URL hình ảnh không được để trống");
    }

    @Test
    void saveImageFromUrlShouldThrowWhenProtocolIsInvalid() {
        assertThatThrownBy(() -> service.saveImageFromUrl("ftp://example.com/image.png", "subjects"))
                .isInstanceOf(AppException.class)
                .hasMessage("Chỉ hỗ trợ URL giao thức HTTP hoặc HTTPS");
    }

    @Test
    void loadFileAsResourceShouldThrowWhenFileNotFound() {
        assertThatThrownBy(() -> service.loadFileAsResource("subjects", "non-existing.png"))
                .isInstanceOf(AppException.class)
                .hasMessage("Không tìm thấy tập tin");
    }
}

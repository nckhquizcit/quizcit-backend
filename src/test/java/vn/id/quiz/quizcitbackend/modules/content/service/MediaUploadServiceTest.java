package vn.id.quiz.quizcitbackend.modules.content.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.Url;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaUploadServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    private MediaUploadService service;

    @BeforeEach
    void setUp() {
        service = new MediaUploadService(cloudinary);
    }

    @Test
    void saveImageShouldUploadToCloudinaryAndReturnSecureUrl() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-subject.png",
                "image/png",
                new byte[]{1, 2, 3, 4}
        );

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(Map.of(
                "secure_url", "https://res.cloudinary.com/demo/image/upload/v12345/quizcit/subjects/test-subject.png",
                "public_id", "quizcit/subjects/test-subject"
        ));

        String url = service.saveImage(file, "subjects");

        assertThat(url).isEqualTo("https://res.cloudinary.com/demo/image/upload/v12345/quizcit/subjects/test-subject.png");
    }

    @Test
    void saveMediaShouldSupportVideoUpload() throws IOException {
        MockMultipartFile videoFile = new MockMultipartFile(
                "file",
                "intro-video.mp4",
                "video/mp4",
                new byte[]{10, 20, 30}
        );

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(Map.of(
                "secure_url", "https://res.cloudinary.com/demo/video/upload/v12345/quizcit/theories/intro-video.mp4",
                "public_id", "quizcit/theories/intro-video"
        ));

        String url = service.saveMedia(videoFile, "theories");

        assertThat(url).isEqualTo("https://res.cloudinary.com/demo/video/upload/v12345/quizcit/theories/intro-video.mp4");
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
    void saveImageFromUrlShouldUploadDirectlyToCloudinary() throws Exception {
        String sourceUrl = "https://example.com/banner.jpg";

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(eq(sourceUrl), anyMap())).thenReturn(Map.of(
                "secure_url", "https://res.cloudinary.com/demo/image/upload/v12345/quizcit/subjects/banner.jpg",
                "public_id", "quizcit/subjects/banner"
        ));

        String url = service.saveImageFromUrl(sourceUrl, "subjects");

        assertThat(url).isEqualTo("https://res.cloudinary.com/demo/image/upload/v12345/quizcit/subjects/banner.jpg");
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
    void getCloudinaryMediaUrlShouldGenerateCloudinaryUrl() {
        Url urlMock = mock(Url.class);
        when(cloudinary.url()).thenReturn(urlMock);
        when(urlMock.secure(true)).thenReturn(urlMock);
        when(urlMock.generate(anyString())).thenReturn("https://res.cloudinary.com/demo/image/upload/quizcit/subjects/sample.webp");

        String url = service.getCloudinaryMediaUrl("subjects", "sample.webp");

        assertThat(url).isEqualTo("https://res.cloudinary.com/demo/image/upload/quizcit/subjects/sample.webp");
    }
}

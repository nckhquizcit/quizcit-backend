package vn.id.quiz.quizcitbackend.shared.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Value("${quizcit.cloudinary.url:}")
    private String cloudinaryUrl;

    @Bean
    public Cloudinary cloudinary() {
        if (cloudinaryUrl != null && !cloudinaryUrl.isBlank()) {
            return new Cloudinary(cloudinaryUrl);
        }
        String envUrl = System.getenv("CLOUDINARY_URL");
        if (envUrl != null && !envUrl.isBlank()) {
            return new Cloudinary(envUrl);
        }
        return new Cloudinary();
    }
}

package vn.id.quiz.quizcitbackend.modules.content;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import vn.id.quiz.quizcitbackend.core.module.AppModule;
@Getter
@Configuration
public class ContentModule implements AppModule {
    private final String name = "Content Module";
    @Override
    public void initialize() {

    }
}

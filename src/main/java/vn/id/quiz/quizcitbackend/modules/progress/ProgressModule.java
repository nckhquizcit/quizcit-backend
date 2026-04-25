package vn.id.quiz.quizcitbackend.modules.progress;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import vn.id.quiz.quizcitbackend.core.module.AppModule;
@Getter
@Configuration
public class ProgressModule implements AppModule {
    private final String name = "Progress Module";

    @Override
    public void initialize() {

    }
}

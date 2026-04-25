package vn.id.quiz.quizcitbackend.modules.quiz;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import vn.id.quiz.quizcitbackend.core.module.AppModule;
@Getter
@Configuration
public class QuizModule implements AppModule {
    private final String name = "Quiz Module";

    @Override
    public void initialize() {

    }
}
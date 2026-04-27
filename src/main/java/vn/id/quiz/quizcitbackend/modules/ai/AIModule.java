package vn.id.quiz.quizcitbackend.modules.ai;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import vn.id.quiz.quizcitbackend.core.module.AppModule;

@Getter
@Configuration
public class AIModule implements AppModule
{
    private final String name = "AI Module";

    @Override
    public void initialize() {

    }
}

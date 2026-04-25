package vn.id.quiz.quizcitbackend.modules.auth;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import vn.id.quiz.quizcitbackend.core.module.AppModule;
@Getter
@Configuration
public class AuthModule implements AppModule {
    private final String name = "Auth Module";

    @Override
    public void initialize() {

    }
}

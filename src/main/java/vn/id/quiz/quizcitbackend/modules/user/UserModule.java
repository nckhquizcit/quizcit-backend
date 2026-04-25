package vn.id.quiz.quizcitbackend.modules.user;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import vn.id.quiz.quizcitbackend.core.module.AppModule;
@Getter
@Configuration
public class UserModule implements AppModule {
    private final String name = "User Module";

    @Override
    public void initialize() {

    }
}

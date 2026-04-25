package vn.id.quiz.quizcitbackend.modules.schedule;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import vn.id.quiz.quizcitbackend.core.module.AppModule;
@Getter
@Configuration
public class ScheduleModule implements AppModule {
    private final String name = "Schedule Module";

    @Override
    public void initialize() {

    }
}

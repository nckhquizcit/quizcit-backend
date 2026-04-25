package vn.id.quiz.quizcitbackend.modules.contribution;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import vn.id.quiz.quizcitbackend.core.module.AppModule;
@Getter
@Configuration
public class ContributionModule implements AppModule {
    private final String name = "Contribution Module";

    @Override
    public void initialize() {

    }
}

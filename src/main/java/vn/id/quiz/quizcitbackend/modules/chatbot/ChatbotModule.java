package vn.id.quiz.quizcitbackend.modules.chatbot;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import vn.id.quiz.quizcitbackend.core.module.AppModule;

@Getter
@Configuration
public class ChatbotModule implements AppModule {
    private final String name = "Chatbot Module";

    @Override
    public void initialize() {

    }
}

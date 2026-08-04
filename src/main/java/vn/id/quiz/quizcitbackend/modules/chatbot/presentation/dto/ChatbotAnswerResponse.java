package vn.id.quiz.quizcitbackend.modules.chatbot.presentation.dto;

import java.util.List;

public record ChatbotAnswerResponse(
        String answer,
        List<ChatSourceResponse> sources,
        boolean hasUnlockedContent) {
}

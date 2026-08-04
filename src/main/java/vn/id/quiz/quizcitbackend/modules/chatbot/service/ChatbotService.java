package vn.id.quiz.quizcitbackend.modules.chatbot.service;

import vn.id.quiz.quizcitbackend.modules.chatbot.presentation.dto.AskChatbotRequest;
import vn.id.quiz.quizcitbackend.modules.chatbot.presentation.dto.ChatbotAnswerResponse;

public interface ChatbotService {

    ChatbotAnswerResponse ask(String userId, AskChatbotRequest request);
}

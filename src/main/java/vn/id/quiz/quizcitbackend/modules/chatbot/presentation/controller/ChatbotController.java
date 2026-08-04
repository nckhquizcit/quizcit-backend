package vn.id.quiz.quizcitbackend.modules.chatbot.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.id.quiz.quizcitbackend.modules.chatbot.presentation.dto.AskChatbotRequest;
import vn.id.quiz.quizcitbackend.modules.chatbot.presentation.dto.ChatbotAnswerResponse;
import vn.id.quiz.quizcitbackend.modules.chatbot.service.ChatbotService;
import vn.id.quiz.quizcitbackend.shared.security.CustomUserPrincipal;
import vn.id.quiz.quizcitbackend.shared.security.PrincipalAccess;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/ask")
    public ResponseEntity<ApiResponse<ChatbotAnswerResponse>> ask(
            @Valid @RequestBody AskChatbotRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        PrincipalAccess.requirePrincipal(principal);
        ChatbotAnswerResponse response = chatbotService.ask(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

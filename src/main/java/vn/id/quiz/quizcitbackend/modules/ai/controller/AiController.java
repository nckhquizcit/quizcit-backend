package vn.id.quiz.quizcitbackend.modules.ai.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.ai.service.AIService;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @PostMapping("/estimate")
    public double estimate() {

        return aiService.estimateTheta(
                List.of(1, 0, 1, 1),
                List.of(0.2, -0.5, 0.1, 1.2),
                List.of(1.0, 1.0, 1.0, 1.0));
    }
}

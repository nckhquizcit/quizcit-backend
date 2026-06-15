package vn.id.quiz.quizcitbackend.modules.ai.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.ai.infrastructure.RaschClient;

@RestController
@RequiredArgsConstructor
public class AiController {

    private final RaschClient raschClient;

    @GetMapping("/test-ai")
    public Double test() {

        return raschClient.estimateTheta(
                List.of(1, 0, 1, 1),
                List.of(0.2, -0.5, 1.0, 0.3),
                List.of(0.8, 0.3, 1.0, 0.7));
    }
}
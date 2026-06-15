package vn.id.quiz.quizcitbackend.modules.ai.service;

import java.util.List;

import org.springframework.stereotype.Service;
import vn.id.quiz.quizcitbackend.modules.ai.dto.*;
import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.ai.client.AIClient;

@Service
@RequiredArgsConstructor
public class AIService {

    private final AIClient aiClient;

    public double estimateTheta(
            List<Integer> responses,
            List<Double> difficulties,
            List<Double> weights) {

        return aiClient.estimateTheta(
                new EstimateThetaRequest(
                        responses,
                        difficulties,
                        weights));
    }
}